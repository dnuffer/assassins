package com.nbs.client.assassins.views;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class TimePickerDialogFragment extends DialogFragment implements OnTimeSetListener {

	public interface OnTimePickedListener {
		public void onTimePicked(int hourOfDay, int minute);
	}
	
	private OnTimePickedListener mListener;
	
	public void setOnTimePickedListener(OnTimePickedListener listener) {
		mListener = listener;
	}
	public static DialogFragment newInstance(String title) {
		DialogFragment frag = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
    	String title = getArguments().getString("title");
    	Time t = new Time();
    	t.setToNow();
        
    	int hour = (t.minute > 56) ? ((t.hour > 22) ? 0 : t.hour + 1) : t.hour;
    	int minute = (t.minute > 56) ? 0 : t.minute + 1;
    	
    	TimePickerDialog dialog = 
        	new TimePickerDialog(getActivity(), this, hour, minute, false);
        dialog.setTitle(title);
        return dialog;
    }
    
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mListener.onTimePicked(hourOfDay, minute);
	}
}
