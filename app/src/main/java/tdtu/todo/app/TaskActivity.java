package tdtu.todo.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;


public class TaskActivity extends AppCompatActivity {
    private static  final  String TAG="HomeActivity";

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private ProgressDialog loader;

    private String key="";
    private String task;
    private String description;
    private String status="0";
    private TextView dateText;
    TextView mDisplayDate;
    CheckBox checkBox;
    String date;


    private DatePickerDialog.OnDateSetListener mDateSetListener;

    public TaskActivity() {
    }

    public class Struc {
        String nTask;
        String nDes;
        String nDate;

        public Struc(String nTask, String nDes, String nDate) {
            this.nTask = nTask;
            this.nDes = nDes;
            this.nDate = nDate;
        }
    }

    Struc myStruct[]=new Struc[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        toolbar=findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Task Todo");

        recyclerView=findViewById(R.id.recycleView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        loader=new ProgressDialog(this);

        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intent=new Intent(this, LoginScreen.class);
            startActivity(intent);
            finish();
        }

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        onlineUserID=mUser.getUid();
        reference= FirebaseDatabase.getInstance().getReference().child("mytask").child(onlineUserID);

        floatingActionButton=findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });
        CheckBox chk=findViewById(R.id.chkComplete);

        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
    }
    private void addTask(){

        AlertDialog.Builder myDialog=new AlertDialog.Builder(this);
        LayoutInflater inflater=LayoutInflater.from(this);
        View myView=inflater.inflate(R.layout.input_file,null);
        myDialog.setView(myView);
        final AlertDialog dialog=myDialog.create();
        dialog.setCancelable(false);
        dialog.show();

        final EditText task=myView.findViewById(R.id.task);
        final EditText description=myView.findViewById(R.id.description);
        Button save=myView.findViewById(R.id.saveBtn);
        Button cancel=myView.findViewById(R.id.cancleBtn);

        final TextView mDisplayDate=myView.findViewById(R.id.tvDatePick);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal=Calendar.getInstance();
                int year=cal.get(Calendar.YEAR);
                int month=cal.get(Calendar.MONTH);
                int day=cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog=new DatePickerDialog(TaskActivity.this,android.R.style.Theme_Holo_Dialog_MinWidth,mDateSetListener,year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();;
            }
        });
        mDateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month=month+1;
                String date=dayOfMonth+"/"+month+"/"+year;
                mDisplayDate.setText(date);
                Log.d(TAG, "onDateSet: date"+dayOfMonth+"/"+month+"/"+year);
            }
        };

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();

                String date= mDisplayDate.getText().toString().trim();
                String mTask=task.getText().toString().trim();
                String mDescription=description.getText().toString().trim();
                String id=reference.push().getKey();
                String status="0";

                if (TextUtils.isEmpty((mTask))){
                    task.setError("Task is empty!");
                    return;
                }
                if (TextUtils.isEmpty(mDescription)){
                    description.setError("Description is empty!");
                    return;
                } else{
                    loader.setMessage("Adding your data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    Model model=new Model(mTask,mDescription,id,date,status);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(TaskActivity.this,"Task has been inserted successfully",Toast.LENGTH_LONG).show();
                                loader.dismiss();
                                dialog.dismiss();
                            }else{
                                String error=task.getException().toString();
                                Toast.makeText(TaskActivity.this,"Fail: "+error,Toast.LENGTH_LONG).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.homeScreen: {
                    Intent intent = new Intent(TaskActivity.this, HomeScreen.class);
                    startActivity(intent);
                    return true;
                }
            }
            return false;
        }
    };

    private void updateTask(){
        AlertDialog.Builder myDialog=new AlertDialog.Builder(this);
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.update_data,null);

        myDialog.setView(view);
        final AlertDialog dialog=myDialog.create();

        final EditText mTask=view.findViewById(R.id.mEditTextTask);
        final EditText mDescription=view.findViewById(R.id.mEditTextDescription);

        mTask.setText(task);
        mTask.setSelection(task.length());

        mDescription.setText(description);
        mDescription.setSelection(description.length());

        Button delButton=view.findViewById(R.id.btnDelete);
        Button updateButton=view.findViewById(R.id.btnUpdate);

        mDisplayDate=view.findViewById(R.id.tvDatePick);
        mDisplayDate.setText(date);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal=Calendar.getInstance();
                int year=cal.get(Calendar.YEAR);
                int month=cal.get(Calendar.MONTH);
                int day=cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog=new DatePickerDialog(TaskActivity.this,android.R.style.Theme_Holo_Dialog_MinWidth,mDateSetListener,year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();;
            }
        });

        mDateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month=month+1;
                String date=dayOfMonth+"/"+month+"/"+year;
                mDisplayDate.setText(date);
                Log.d(TAG, "onDateSet: date"+dayOfMonth+"/"+month+"/"+year);
            }
        };

        checkBox=view.findViewById(R.id.chkCompleteUpdate);
        if (status.equals("1")) {
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date= mDisplayDate.getText().toString().trim();
                task=mTask.getText().toString().trim();
                description=mDescription.getText().toString().trim();
                if (checkBox.isChecked()){
                    status="1";
                }
                else {
                    status="0";
                }
                Model model=new Model(task,description,key,date,status);

                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(TaskActivity.this,"Data has been updated successfully!",Toast.LENGTH_LONG).show();
                        }else{
                            String err=task.getException().toString();
                            Toast.makeText(TaskActivity.this,"Updated failed!-",Toast.LENGTH_LONG).show();

                        }
                    }
                });
                dialog.dismiss();
            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(TaskActivity.this,"Task has been deleted successfully",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(TaskActivity.this,"Deleted has been failed!",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        final FirebaseRecyclerOptions<Model> options=new FirebaseRecyclerOptions.Builder<Model>().setQuery(reference,Model.class).build();
        FirebaseRecyclerAdapter<Model,MyViewHolder> adapter=new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Model model) {
                System.out.println("__________________________________________________________");
                myStruct[position] =new Struc(model.getTask(),model.getDescription(),model.getDate());

                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDesc(model.getDescription());
                holder.setStatus(model.getStatus());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key=getRef(position).getKey();
                        task=model.getTask();
                        description=model.getDescription();
                        status=model.getStatus();
                        date=model.getDate();

                        updateTask();
                    }
                });
            }
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_layout,parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            mView=itemView;
        }
        public void setStatus(String task){
            CheckBox chkBox=mView.findViewById(R.id.chkComplete);
            if (task.equals("1")) {
                chkBox.setChecked(true);
            }
            else{
                chkBox.setChecked(false);
            }
        }

        public void setTask(String task){
            TextView taskTextView=mView.findViewById(R.id.taskTv);
            taskTextView.setText(task);
        }
        public void setDesc(String desc){
            TextView tasDescView=mView.findViewById(R.id.descriptionTv);
            tasDescView.setText(desc);
        }
        public void setDate(String date){
            TextView dateTextView=mView.findViewById(R.id.dateTv);
            dateTextView.setText(date);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.homeScreen: {
                Intent intent = new Intent(TaskActivity.this, HomeScreen.class);
                startActivity(intent);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

}
