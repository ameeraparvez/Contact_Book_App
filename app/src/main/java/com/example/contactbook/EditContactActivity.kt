package com.example.contactbook

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.contactbook.databinding.ActivityEditContactBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding
    private lateinit var dbRef: DatabaseReference

    private var contactId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Contacts")

        contactId = intent.getStringExtra("id") ?: ""

        binding.etName.setText(intent.getStringExtra("name"))
        binding.etPhone.setText(intent.getStringExtra("phone"))
        binding.etEmail.setText(intent.getStringExtra("email"))

        binding.btnUpdate.setOnClickListener {
            validateAndUpdate()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun validateAndUpdate() {

        val updatedName = binding.etName.text.toString().trim()
        val updatedPhone = binding.etPhone.text.toString().trim()
        val updatedEmail = binding.etEmail.text.toString().trim()

        when {

            updatedName.isEmpty() -> {
                binding.etName.error = "Enter name"
                binding.etName.requestFocus()
                return
            }

            updatedPhone.isEmpty() -> {
                binding.etPhone.error = "Enter phone number"
                binding.etPhone.requestFocus()
                return
            }

            updatedPhone.length < 10 -> {
                binding.etPhone.error = "Enter valid phone number"
                binding.etPhone.requestFocus()
                return
            }

            updatedEmail.isNotEmpty() &&
                    !Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches() -> {

                binding.etEmail.error = "Enter valid email"
                binding.etEmail.requestFocus()
                return
            }
        }

        updateContact(
            updatedName,
            updatedPhone,
            updatedEmail
        )
    }

    private fun updateContact(
        name: String,
        phone: String,
        email: String
    ) {

        binding.btnUpdate.isEnabled = false
        binding.btnUpdate.text = "Updating..."

        val updatedContact = Contact(
            id = contactId,
            name = name,
            phone = phone,
            email = email
        )

        dbRef.child(contactId)
            .setValue(updatedContact)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Contact updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener {

                binding.btnUpdate.isEnabled = true
                binding.btnUpdate.text = "Update Contact"

                Toast.makeText(
                    this,
                    "Failed to update contact",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDeleteDialog() {

        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")
            .setPositiveButton("Delete") { _, _ ->
                deleteContact()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteContact() {

        binding.btnDelete.isEnabled = false
        binding.btnDelete.text = "Deleting..."

        dbRef.child(contactId)
            .removeValue()
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Contact deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener {

                binding.btnDelete.isEnabled = true
                binding.btnDelete.text = "Delete Contact"

                Toast.makeText(
                    this,
                    "Failed to delete contact",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}