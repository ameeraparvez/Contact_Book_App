package com.example.contactbook

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactbook.databinding.ActivityHomeBinding
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ContactAdapter
    private lateinit var dbRef: DatabaseReference

    private val contactList = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Contacts")

        adapter = ContactAdapter(contactList) { contact ->

            val intent = Intent(this, EditContactActivity::class.java).apply {
                putExtra("id", contact.id)
                putExtra("name", contact.name)
                putExtra("phone", contact.phone)
                putExtra("email", contact.email)
                putExtra("image", contact.image)
            }

            startActivity(intent)
        }

        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewContacts.adapter = adapter

        fetchContacts()

        setupSwipeActions()

        binding.etSearch.addTextChangedListener { text ->
            adapter.filter(text.toString())
        }

        binding.fabAddContact.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddContactActivity::class.java
                )
            )
        }

        binding.fabDialPad.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            startActivity(intent)
        }
    }

    private fun fetchContacts() {

        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                contactList.clear()

                for (data in snapshot.children) {

                    val contact =
                        data.getValue(Contact::class.java)

                    contact?.let {
                        contactList.add(it)
                    }
                }

                adapter.updateData(contactList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupSwipeActions() {

        val swipeHandler =
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    rv: RecyclerView,
                    vh: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {

                    val position =
                        viewHolder.bindingAdapterPosition

                    if (position ==
                        RecyclerView.NO_POSITION
                    ) return

                    val contact =
                        adapter.getContactAt(position)

                    when (direction) {

                        ItemTouchHelper.LEFT -> {

                            contact.id?.let { id ->
                                dbRef.child(id)
                                    .removeValue()
                            }
                        }

                        ItemTouchHelper.RIGHT -> {

                            val intent =
                                Intent(Intent.ACTION_DIAL)
                                    .apply {
                                        data = Uri.parse(
                                            "tel:${contact.phone}"
                                        )
                                    }

                            startActivity(intent)

                            adapter.notifyItemChanged(
                                position
                            )
                        }
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {

                    val itemView = viewHolder.itemView

                    val backgroundPaint = Paint()

                    val textPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 45f
                        isFakeBoldText = true
                    }

                    if (dX > 0) {

                        backgroundPaint.color =
                            Color.parseColor("#4CAF50")

                        c.drawRect(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            itemView.left + dX,
                            itemView.bottom.toFloat(),
                            backgroundPaint
                        )

                        c.drawText(
                            "CALL",
                            itemView.left + 80f,
                            itemView.top +
                                    itemView.height / 2f + 15,
                            textPaint
                        )

                    } else if (dX < 0) {

                        backgroundPaint.color =
                            Color.parseColor("#F44336")

                        c.drawRect(
                            itemView.right + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat(),
                            backgroundPaint
                        )

                        c.drawText(
                            "DELETE",
                            itemView.right - 240f,
                            itemView.top +
                                    itemView.height / 2f + 15,
                            textPaint
                        )
                    }

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        ItemTouchHelper(swipeHandler)
            .attachToRecyclerView(
                binding.recyclerViewContacts
            )
    }
}