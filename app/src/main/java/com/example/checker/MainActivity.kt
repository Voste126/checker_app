package com.example.checker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldtransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearlayoutManager: LinearLayoutManager
    private lateinit var db:AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactions= arrayListOf(
        )
        transactionAdapter= TransactionAdapter(transactions)
        linearlayoutManager= LinearLayoutManager(this)

        db=Room.databaseBuilder(this,
        AppDatabase::class.java,
        "transactions").build()

        val recyclerview:RecyclerView
        recyclerview=findViewById(R.id.recyclerview)
        recyclerview.apply {
            adapter=transactionAdapter
            layoutManager= linearlayoutManager
        }

        updatedashboard()


        //swipe to delete
        val itemtouchHelper=object :ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }
        val swipeHelper=ItemTouchHelper(itemtouchHelper)
        swipeHelper.attachToRecyclerView(recyclerview)

        val addbtn:FloatingActionButton
        addbtn=findViewById(R.id.addbtn)
        addbtn.setOnClickListener {
            val intent=Intent(this,AddTransactionActivity::class.java)
            startActivity(intent)
        }


    }
    private fun fetchAll(){
GlobalScope.launch {
    transactions=db.transactionDao().getAll()

   runOnUiThread {
      updatedashboard()
       transactionAdapter.setData(transactions)

   }

}
    }
    private fun updatedashboard(){
        val totalAmount=transactions.map { it.amount }.sum()
        val budgetAmount=transactions.filter { it.amount>0 }.map { it.amount }.sum()
        val expenseAmount=totalAmount-budgetAmount

        val balance:TextView
        balance=findViewById(R.id.balance)
        balance.text="ksh%.2f".format(totalAmount)
        val budget:TextView
        budget=findViewById(R.id.budget)
        budget.text="ksh%.2f".format(budgetAmount)
        val expense:TextView
        expense=findViewById(R.id.expense)
        expense.text="ksh%.2f".format(expenseAmount)
    }
    private fun undodeleted(){
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)
            transactions=oldtransactions
            runOnUiThread {
                transactionAdapter.setData(transactions)
                updatedashboard()

            }
        }
    }
    private fun showSnackbar(){
        val view =findViewById<View>(R.id.coordinator)
        val snackbar =Snackbar.make(view,"Transactiondeleted!",Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undodeleted()
        }
            .setActionTextColor(ContextCompat.getColor(this,R.color.red))
            .setActionTextColor(ContextCompat.getColor(this,R.color.white))
            .show()
    }



    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction=transaction
        oldtransactions=transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)
            transactions=transactions.filter { it.id!=transaction.id }

            runOnUiThread {
                updatedashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}