package com.example.contactbook

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.contactbook.databinding.ActivityAddContactBinding
import com.google.firebase.database.FirebaseDatabase

class AddContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactBinding
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button

    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("Contacts")
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivContactImg.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ivContactImg.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnSave.setOnClickListener {

            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name & Phone required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contactId = ref.push().key

            if (contactId == null) {
                Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contact = Contact(
                id = contactId,
                name = name,
                phone = phone,
                email = email,
                image = selectedImageUri?.toString() ?: ""
            )

            ref.child(contactId).setValue(contact)
                .addOnSuccessListener {
                    Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to Save", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
