package com.example.unisehat.temu5

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unisehat.R

class AlertDialogActivity : AppCompatActivity() {
    private var closeButton: Button? = null
    private var builder: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_dialog)

        closeButton = findViewById(R.id.button)
        builder = AlertDialog.Builder(this)

        closeButton?.setOnClickListener {
            //Uncomment the below code to Set the message and title from the strings.xml file
            // builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
            // Setting message manually and performing action on button click
            builder?.setMessage("Do you want to close this application?")
                ?.setCancelable(false)
                ?.setPositiveButton("Yes") { dialog, id ->
                    finish()
                    Toast.makeText(
                        applicationContext, "You chose yes action for alertbox",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                ?.setNegativeButton("No") { dialog, id -> // Action for 'NO' Button
                    dialog.cancel()
                    Toast.makeText(
                        applicationContext, "You chose no action for alertbox",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            // Creating dialog box
            val alert = builder?.create()
            // Setting the title manually
            alert?.setTitle("AlertDialogExample")
            alert?.show()
        }
    }
}
