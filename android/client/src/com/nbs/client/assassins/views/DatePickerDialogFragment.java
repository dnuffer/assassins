package com.nbs.client.assassins.views;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerDialogFragment extends DialogFragment implements OnDateSetListener {

	public interface OnDatePickedListener {
		public void onDatePicked(int year, int monthOfYear,
				int dayOfMonth);
	}
	
	private OnDatePickedListener mListener;
	
	public void setOnDatePickedListener(OnDatePickedListener listener) {
		mListener = listener;
	}
	public static DialogFragment newInstance(String title) {
		DialogFragment frag = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
    	String title = getArguments().getString("title");
    	final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = 
        	new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.setTitle(title);
        return dialog;
    }
    
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		if(mListener != null) {
			mListener.onDatePicked(year, monthOfYear, dayOfMonth);
		}
	}
}
