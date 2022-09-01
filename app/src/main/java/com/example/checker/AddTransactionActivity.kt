package com.example.checker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    lateinit var addtransactionbtn:Button
    lateinit var labelinput:EditText
    lateinit var amountinput:EditText
    lateinit var descriptioninput:EditText
    lateinit var closebtn:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        addtransactionbtn=findViewById(R.id.addtransactionbtn)
       labelinput=findViewById(R.id.labelinput)
       amountinput=findViewById(R.id.amountinput)
       descriptioninput=findViewById(R.id.descriptioninput)
        closebtn=findViewById(R.id.closebtn)

        labelinput.addTextChangedListener {
            if (it!!.count()>0)
                labelinput.error=null
        }
        amountinput.addTextChangedListener {
            if (it!!.count()>0)
                amountinput.error=null
        }

        addtransactionbtn.setOnClickListener {
           val label = labelinput.text.toString()
            val amount=amountinput.text.toString().toDoubleOrNull()
            val description=descriptioninput.text.toString()

            if (label.isEmpty())
                labelinput.error="please enter a valid label"
            else if (amount==null)
                amountinput.error="please enter a valid amount"
            else{

             val transaction =Transaction(0,label, amount, description)
                insert(transaction)
            }
        }
        closebtn.setOnClickListener {
            finish()
        }
    }
    private fun insert(transaction: Transaction){
        val db:AppDatabase=Room.databaseBuilder(this,
        AppDatabase::class.java,
        "transactions").build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}