package com.example.contactbook

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.contactbook.databinding.ItemContactBinding

class ContactAdapter(
    private var contactList: MutableList<Contact>,
    private val onClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var filteredList: MutableList<Contact> = contactList.toMutableList()

    class ContactViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {

        val contact = filteredList[position]

        holder.binding.tvName.text = contact.name
        holder.binding.tvPhone.text = contact.phone

        if (!contact.image.isNullOrEmpty()) {
            holder.binding.imgProfile.setImageURI(Uri.parse(contact.image))
        } else {
            holder.binding.imgProfile.setImageResource(R.drawable.person_ic)
        }

        holder.itemView.setOnClickListener {
            onClick(contact)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {

        filteredList = if (query.isEmpty()) {
            contactList.toMutableList()
        } else {
            contactList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query)
            }.toMutableList()
        }

        notifyDataSetChanged()
    }

    fun updateData(newList: MutableList<Contact>) {
        contactList = newList
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun getContactAt(position: Int): Contact {
        return filteredList[position]
    }

    fun removeAt(position: Int) {

        val contact = filteredList[position]

        contactList.remove(contact)
        filteredList.removeAt(position)

        notifyItemRemoved(position)
    }
}