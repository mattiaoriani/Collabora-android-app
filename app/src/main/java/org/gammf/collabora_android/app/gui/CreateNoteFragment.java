package org.gammf.collabora_android.app.gui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import org.gammf.collabora_android.app.R;
import org.gammf.collabora_android.notes.Note;
import org.gammf.collabora_android.app.SendMessageToServerTask;
import org.gammf.collabora_android.communication.update.general.UpdateMessage;
import org.gammf.collabora_android.communication.update.general.UpdateMessageType;
import org.gammf.collabora_android.communication.update.notes.ConcreteNoteUpdateMessage;
import org.gammf.collabora_android.notes.Location;
import org.gammf.collabora_android.notes.Note;
import org.gammf.collabora_android.notes.NoteState;
import org.gammf.collabora_android.notes.SimpleNoteBuilder;
import org.gammf.collabora_android.notes.State;
import org.joda.time.DateTime;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateNoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateNoteFragment extends Fragment implements PlaceSelectionListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SENDER = "sender";
    private static final String ARG_COLLABNAME = "collabName";
    private static final String ARG_COLLABTYPE = "collabType";
    private static final String ARG_COLLABORATION_ID = "COLLABORATION_ID";
    private static final String ARG_MODULENAME = "moduleName";

    private SupportPlaceAutocompleteFragment autocompleteFragment;
    private String noteState = "";
    private Calendar calendar;
    private Time clock;
    private TextView dateView, timeView;
    private int year, month, day, hour, minute;
    private EditText txtContentNote;
    private Spinner spinnerState;

    private String sender, collabName, collabType, collaborationId, moduleName;
    //  private OnFragmentInteractionListener mListener;

    public CreateNoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param collabName collaboration name
     * @param collabType collaboration type
     * @param collaborationId collaboration id
     * @return A new instance of fragment CreateNoteFragment.
     */
    public static CreateNoteFragment newInstance(String sender,
                                                 String collabName,
                                                 String collabType,
                                                 String collaborationId,
                                                 String moduleName) {
        Bundle arg = new Bundle();
        arg.putString(ARG_SENDER, sender);
        arg.putString(ARG_COLLABORATION_ID, collaborationId);
        arg.putString(ARG_COLLABNAME, collabName);
        arg.putString(ARG_COLLABTYPE, collabType);
        arg.putString(ARG_MODULENAME, moduleName);
        final CreateNoteFragment fragment = new CreateNoteFragment();
        fragment.setArguments(arg);
        Log.i("Async", "DIO E': " + fragment.getArguments().getString(ARG_COLLABORATION_ID));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            Log.e("Async", "CollaborationId in fragment is: " + getArguments().getString(ARG_COLLABORATION_ID));
            this.sender = getArguments().getString(ARG_SENDER);
            this.collaborationId = getArguments().getString(ARG_COLLABORATION_ID);
            this.collabName = getArguments().getString(ARG_COLLABNAME);
            this.collabType = getArguments().getString(ARG_COLLABTYPE);
            this.moduleName = getArguments().getString(ARG_MODULENAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_note, container, false);
        txtContentNote = rootView.findViewById(R.id.txtNoteContent);
        txtContentNote.requestFocus();
        autocompleteFragment = new SupportPlaceAutocompleteFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.place_autocomplete_fragment, autocompleteFragment);
        ft.commit();
        autocompleteFragment.setOnPlaceSelectedListener(this);

        spinnerState = (Spinner) rootView.findViewById(R.id.spinnerNewNoteState);
        List<NoteProjectState> stateList = new ArrayList<>();
        stateList.addAll(Arrays.asList(NoteProjectState.values()));
        ArrayAdapter<NoteProjectState> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, stateList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(dataAdapter);

        FloatingActionButton btnAddNote = rootView.findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String insertedNoteName = txtContentNote.getText().toString();
                if(insertedNoteName.equals("")){
                    Resources res = getResources();
                    txtContentNote.setError(res.getString(R.string.fieldempty));
                }else {

                    addNote(insertedNoteName, null, new NoteState(noteState, "fone"), null);

                }

            }
        });
        dateView = rootView.findViewById(R.id.txtNewDateSelected);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month+1, day);

        timeView = rootView.findViewById(R.id.txtNewTimeSelected);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String strTime = sdf.format(calendar.getTime());
        timeView.setText(strTime);


        ImageButton btnSetDateExpiration = rootView.findViewById(R.id.btnSetDateExpiration);
        btnSetDateExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),
                        myDateListener, year, month, day).show();
            }
        });

        ImageButton btnSetTimeExpiration = rootView.findViewById(R.id.btnSetTimeExpiration);
        btnSetTimeExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getActivity(),
                        myTimeListener, hour, minute, true).show();
            }
        });
        return rootView;
    }

    private void addNote(final String content, final Location location, final NoteState state, final DateTime expiration){
        CollaborationFragment collabFragment = CollaborationFragment.newInstance("notecreation",collabName, collabType);

        final Note newNote = new SimpleNoteBuilder(content).setLocation(location).setState(state).setExpirationDate(expiration).buildNote();
        final UpdateMessage message = new ConcreteNoteUpdateMessage("fone", newNote, UpdateMessageType.CREATION, collaborationId);
        new SendMessageToServerTask().execute(message);

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, collabFragment).commit();
    }
    @Override
    public void onPlaceSelected(Place place) {
        // TODO: Get info about the selected place.
        Log.i(TAG, "Place: " + place.getName());

        String placeDetailsStr = place.getName()+"";
              /*  + "\n"
                + place.getId() + "\n"
                + place.getLatLng().toString() + "\n"
                + place.getAddress() + "\n"
                + place.getAttributions();*/
    }

    @Override
    public void onError(Status status) {
        Log.i(TAG, "An error occurred: " + status);
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };
    private TimePickerDialog.OnTimeSetListener myTimeListener = new
            TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    showTime(hour,minute);
                }
            };
    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
    private void showTime(int hour, int minute){
        timeView.setText(new StringBuilder().append(hour).append(":").append(minute));
    }
/*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

/*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    */
}
