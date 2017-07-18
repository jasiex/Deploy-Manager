package com.example.janek.deploymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static FirebaseUser user;
    public static String name;
    public static DatabaseReference databaseReference;
    public static String localUser;
    public static ArrayList<Order> clientOrders;
    public static ArrayList<Order> allOrders;
    public static ArrayList<Order> acceptedOrders;
    ChildEventListener clientEventListener;
    ChildEventListener adminEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = FirebaseAuth.getInstance().getCurrentUser();
        clientOrders = new ArrayList<>();
        allOrders = new ArrayList<>();
        acceptedOrders = new ArrayList<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("user");
        FloatingActionButton fabAddOrder = (FloatingActionButton) findViewById(R.id.fabbAddOrder);
        if (user != null) {
            localUser = userName(user.getEmail());
            name = user.getDisplayName();
            if(name.equals("client")){
                fabAddOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, AddOrderActivity.class);
                        startActivity(intent);
                    }
                });
                FragmentTransaction clientFragmentTransaction = getSupportFragmentManager().beginTransaction();
                clientFragmentTransaction.add(R.id.mainContainer, ClientFragment.getInstance(), "client_orders");
                clientFragmentTransaction.commit();

                databaseReference.child(localUser).child("Orders").addChildEventListener(clientEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if(!clientOrders.contains(order)){
                            clientOrders.add(0, order);
                        }
                        Fragments();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }else if (name.equals("admin")){
                fabAddOrder.setVisibility(View.GONE);
                FragmentTransaction adminFragmentTransaction = getSupportFragmentManager().beginTransaction();
                adminFragmentTransaction.add(R.id.mainContainer, AdminFragment.getInstance(), "admin_orders");
                adminFragmentTransaction.commit();

                MainActivity.databaseReference.child("Admin").child(MainActivity.localUser).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if(!acceptedOrders.contains(order)){
                            acceptedOrders.add(0, order);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                databaseReference.child("Admin").child("Orders").addChildEventListener(adminEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if(!allOrders.contains(order)){
                            allOrders.add(0, order);
                        }
                        Fragments();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Order order = dataSnapshot.getValue(Order.class);
                        allOrders.remove(order);
                        Fragments();
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.finish();
            startActivity(intent);
            Toast.makeText(this, "Wylogowano", Toast.LENGTH_SHORT).show();
            return true;
        }
        if(name.equals("admin")){
            if(id == R.id.action_acceptedOrders){
                Intent intent = new Intent(MainActivity.this, AcceptedOrdersActivity.class);
                startActivity(intent);
            }
        }else {
            Toast.makeText(this, "Opcja dla Klientow nie dostępna", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public String userName(String email){
        String[] tmp = email.split("@");
        localUser = tmp[0];
        return localUser;
    }

    public void Fragments(){
        if(name.equals("client")){
            ClientFragment clientFragment = (ClientFragment) getSupportFragmentManager().findFragmentByTag("client_orders");
            clientFragment.adapterClient.orders = clientOrders;
            clientFragment.adapterClient.notifyDataSetChanged();
        }else if(name.equals("admin")){
            AdminFragment adminFragment = (AdminFragment) getSupportFragmentManager().findFragmentByTag("admin_orders");
            adminFragment.adapterAdmin.adminOrders = allOrders;
            adminFragment.adapterAdmin.notifyDataSetChanged();
        }
    }
}
