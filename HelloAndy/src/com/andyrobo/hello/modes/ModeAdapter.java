package com.andyrobo.hello.modes;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andyrobo.hello.R;

public class ModeAdapter extends ArrayAdapter<IMode> {

	private Context context;
	private List<IMode> modes;

	public ModeAdapter(Context context, List<IMode> modes) {
		super(context, R.layout.list_style_modes, modes);
		this.context = context;
		this.modes = modes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			View rowView = inflater.inflate(R.layout.list_style_modes, parent, false);
			
			TextView label = (TextView) rowView.findViewById(R.id.label);
			TextView shortNote = (TextView) rowView.findViewById(R.id.shortnote);
			ImageView logo = (ImageView) rowView.findViewById(R.id.logo);
			
			IMode m = modes.get(position);	
			label.setText(m.getName());
			shortNote.setText(m.getDescription());
			logo.setImageResource(m.getImageResourceID());
			return rowView;
	}

}
