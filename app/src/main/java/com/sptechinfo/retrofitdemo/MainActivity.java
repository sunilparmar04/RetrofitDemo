package com.sptechinfo.retrofitdemo;
/**
 * Created by sunil on 03/09/18.
 */
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.sptechinfo.retrofitdemo.adapter.NotesAdapter;
import com.sptechinfo.retrofitdemo.network.ApiClient;
import com.sptechinfo.retrofitdemo.network.ApiService;
import com.sptechinfo.retrofitdemo.network.model.Note;
import com.sptechinfo.retrofitdemo.network.model.User;
import com.sptechinfo.retrofitdemo.util.MyDividerItemDecoration;
import com.sptechinfo.retrofitdemo.util.PrefUtils;
import com.sptechinfo.retrofitdemo.util.RecyclerTouchListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ApiService apiService;
    public static final String TAG = "retroDemo";
    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();

    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;
    TextView noNotesView;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_title_home));
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.txt_empty_notes_view);

        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        // createUser();
        if (TextUtils.isEmpty(PrefUtils.getApiKey(this))) {
            createUser();
        } else {
            // user is already registered, fetch all notes
            fetchAllNotes();
        }
    }


    private void createUser() {
        String uniqueId = UUID.randomUUID().toString();

        Map<String, String> map = new HashMap<>();

        map.put("device_id", uniqueId);

        Call<User> call = apiService.register(map);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.v(TAG, "response:" + response.body().getApiKey());
                PrefUtils.storeApiKey(MainActivity.this, response.body().getApiKey());

                showAlert(null, "User created successfully");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

    }


    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    deleteNote(notesList.get(position).getId(), position);
                }
            }
        });
        builder.show();
    }

    private void toggleEmptyNotes() {
        if (notesList.size() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }

    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && note != null) {
                    // update note by it's id
                    updateNote(note.getId(), inputNote.getText().toString(), position);
                } else {
                    // create new note
                    createNote(inputNote.getText().toString());
                }
            }
        });
    }


    /**
     * Updating a note
     */
    private void updateNote(int noteId, final String note, final int position) {
        disposable.add(
                apiService.updateNote(noteId, note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d(TAG, "Note updated!");

                                Note n = notesList.get(position);
                                n.setNote(note);

                                // Update item and notify adapter
                                notesList.set(position, n);
                                mAdapter.notifyItemChanged(position);

                                showAlert(null,"Note updated!");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                                showAlert(e, "");
                            }
                        }));
    }

    /**
     * Deleting a note
     */
    private void deleteNote(final int noteId, final int position) {
        Log.e(TAG, "deleteNote: " + noteId + ", " + position);
        disposable.add(
                apiService.deleteNote(noteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d(TAG, "Note deleted! " + noteId);

                                // Remove and notify adapter about item deletion
                                notesList.remove(position);
                                mAdapter.notifyItemRemoved(position);

                                //Toast.makeText(MainActivity.this, "Note deleted!", Toast.LENGTH_SHORT).show();
                                showAlert(null,"Note deleted!");

                                toggleEmptyNotes();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                                showAlert(e, "");
                            }
                        })
        );
    }

    private void createNote(String note) {
        disposable.add(
                apiService.createNote(note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Note>() {

                            @Override
                            public void onSuccess(Note note) {
                                if (!TextUtils.isEmpty(note.getError())) {
                                    Toast.makeText(getApplicationContext(), note.getError(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Log.d(TAG, "new note created: " + note.getId() + ", " + note.getNote() + ", " + note.getTimestamp());

                                // Add new item and notify adapter
                                notesList.add(0, note);
                                mAdapter.notifyItemInserted(0);

                                toggleEmptyNotes();

                                showAlert(null,"Note created!");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                                showAlert(e,null);
                            }
                        }));

   /*     Call<List<Note>> createNote = apiService.createNote(note);
        createNote.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                Log.v(TAG, "create notes:" + response.isSuccessful());


            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {

            }
        });*/
    }


    private void fetchAllNotes() {


        disposable.add(
                apiService.fetchAllNotes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Function<List<Note>, List<Note>>() {
                            @Override
                            public List<Note> apply(List<Note> notes) throws Exception {
                                // TODO - note about sort
                                Collections.sort(notes, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note n1, Note n2) {
                                        return n2.getId() - n1.getId();
                                    }
                                });
                                return notes;
                            }
                        })
                        .subscribeWith(new DisposableSingleObserver<List<Note>>() {
                            @Override
                            public void onSuccess(List<Note> notes) {
                                notesList.clear();
                                notesList.addAll(notes);


                                toggleEmptyNotes();

                                Log.d(TAG, "all notes: "+notes.size());
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                                showAlert(e,"");
                            }
                        })
        );

/*
        Call<List<Note>> fetchNotes = apiService.fetchAllNotes();

        fetchNotes.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                Log.v(TAG, "fetch notes:" + response.isSuccessful());


            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {

            }
        });*/
    }

    private void showAlert(Throwable e, String alertMessage) {


        String message = "";
        try {
            if (!TextUtils.isEmpty(alertMessage)) {
                message = alertMessage;
            } else {
                if (e instanceof IOException) {
                    message = "No internet connection!";
                } else if (e instanceof HttpException) {
                    HttpException error = (HttpException) e;
                    String errorBody = error.response().errorBody().string();
                    JSONObject jObj = new JSONObject(errorBody);

                    message = jObj.getString("error");
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(message)) {
            message = "Unknown error occurred! Check LogCat.";
        }

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
