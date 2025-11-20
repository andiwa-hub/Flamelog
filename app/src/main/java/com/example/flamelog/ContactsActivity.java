package com.example.flamelog;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvContactCount;
    private LinearLayout emptyStateLayout;
    private RecyclerView rvContacts;
    private FloatingActionButton fabAddContact;
    private LinearLayout dialogContainer;
    private EditText etContactName, etContactPhone;
    private LinearLayout btnPriority1, btnPriority2, btnPriority3;
    private Button btnCancel, btnSave;

    // firebase + adapter
    private DatabaseReference contactsRef;
    private ArrayList<Contact> contactList = new ArrayList<>();
    private ContactAdapter adapter;

    private String selectedPriority = "Low";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        btnBack = findViewById(R.id.btnBack);
        tvContactCount = findViewById(R.id.tvContactCount);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        rvContacts = findViewById(R.id.rvContacts);
        fabAddContact = findViewById(R.id.fabAddContact);
        dialogContainer = findViewById(R.id.dialogContainer);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnPriority1 = findViewById(R.id.btnPriority1);
        btnPriority2 = findViewById(R.id.btnPriority2);
        btnPriority3 = findViewById(R.id.btnPriority3);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // firebase reference
        contactsRef = FirebaseDatabase.getInstance()
                .getReference("Contacts");

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList, this::deleteContact);
        rvContacts.setAdapter(adapter);

        // load the contacts
        contactsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                contactList.clear();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    Contact contact = child.getValue(Contact.class);
                    if (contact != null) {
                        contact.setKey(child.getKey());
                        contactList.add(contact);
                    }
                }
                adapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(ContactsActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });


        btnBack.setOnClickListener(v -> finish());


        fabAddContact.setOnClickListener(v -> showDialog());

        btnPriority1.setOnClickListener(v -> selectedPriority = "High");
        btnPriority2.setOnClickListener(v -> selectedPriority = "Medium");
        btnPriority3.setOnClickListener(v -> selectedPriority = "Low");

        btnCancel.setOnClickListener(v -> dialogContainer.setVisibility(View.GONE));

        btnSave.setOnClickListener(v -> saveContact());
    }

    private void updateUI() {
        if (contactList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
            tvContactCount.setText("0 contacts • Tap + to add");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
            tvContactCount.setText(contactList.size() + " contacts");
        }
    }

    private void showDialog() {
        dialogContainer.setVisibility(View.VISIBLE);
        etContactName.setText("");
        etContactPhone.setText("");
        selectedPriority = "Low";
    }

    private void saveContact() {
        String name = etContactName.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Enter name and phone", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = contactsRef.push().getKey();
        Contact contact = new Contact(name, phone, selectedPriority);
        contactsRef.child(key).setValue(contact);

        dialogContainer.setVisibility(View.GONE);
    }

    private void deleteContact(Contact contact) {
        if (contact.getKey() != null) {
            contactsRef.child(contact.getKey()).removeValue();
        }
    }

    // contact model
    public static class Contact {
        private String key;
        private String name;
        private String phone;
        private String priority;

        public Contact() {}
        public Contact(String name, String phone, String priority) {
            this.name = name;
            this.phone = phone;
            this.priority = priority;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getPriority() { return priority; }
    }

    // adapter
    private static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
        private ArrayList<Contact> contacts;
        private OnDeleteClickListener deleteClickListener;

        public interface OnDeleteClickListener {
            void onDelete(Contact contact);
        }

        ContactAdapter(ArrayList<Contact> contacts, OnDeleteClickListener listener) {
            this.contacts = contacts;
            this.deleteClickListener = listener;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {
            Contact contact = contacts.get(position);
            TextView text1 = holder.itemView.findViewById(android.R.id.text1);
            TextView text2 = holder.itemView.findViewById(android.R.id.text2);

            text1.setText(contact.getName());
            text2.setText(contact.getPhone() + " • " + contact.getPriority());

            holder.itemView.setOnLongClickListener(v -> {
                deleteClickListener.onDelete(contact);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            ContactViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}

