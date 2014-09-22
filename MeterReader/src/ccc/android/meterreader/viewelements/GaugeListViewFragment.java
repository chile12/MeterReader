package ccc.android.meterreader.viewelements;

import ccc.android.meterdata.types.Gauge;
import ccc.android.meterdata.types.Reading;
import ccc.android.meterreader.MainActivity;
import ccc.android.meterreader.R;
import ccc.android.meterreader.helpfuls.EpviMeterListMapper;
import ccc.android.meterreader.statics.Statics;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView.OnEditorActionListener;

public class GaugeListViewFragment extends Fragment
{
	private MainActivity context;
    private ExpandableListView stationsELV;
    private EpviExpandeableListViewAdapter elvAdapter;
	private EpviMeterListMapper mapper;
	private boolean groupSpinnerInitialized = false;
	private boolean filterSpinnerInitialized = false;

	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		this.mapper = new EpviMeterListMapper(context);
        elvAdapter = new EpviExpandeableListViewAdapter(context);
        elvAdapter.setMapper(mapper, 0);
        this.getResources().updateConfiguration(Statics.getConfiguration(), this.getResources().getDisplayMetrics());
    }
	
	@Override
    public void onStart() 
    {
		super.onStart();
        ((Spinner) getView().findViewById(R.id.groupByDD)).setOnItemSelectedListener(groupSpinnerListener);
        ((Spinner) getView().findViewById(R.id.filterDD)).setOnItemSelectedListener(filterSpinnerListener);
        stationsELV = (ExpandableListView) getView().findViewById(R.id.stationsELV);
        stationsELV.setOnChildClickListener(elvChildClickListener);
        stationsELV.setOnItemLongClickListener(elvLongClickListener);
        stationsELV.setOnGroupClickListener(elvGroupClickListener);
        ((Button) getView().findViewById(R.id.epviListOptionsBT)).setOnClickListener(elvOtionsViewListener);
        ((Button) getView().findViewById(R.id.showListViewBT)).setOnClickListener(elvShowViewListener);
        ((Button) getView().findViewById(R.id.resetListViewBT)).setOnClickListener(elvResetViewListener);
        ((EditText) getView().findViewById(R.id.filterListTB)).setOnEditorActionListener(epviListTBListener);
        stationsELV.setAdapter(elvAdapter);
    }
	
	@Override
    public void onResume() 
    {
		super.onResume();
		setFilterSummaryLA();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.gaugelistviewadapter, container, false);
    }
    

	private OnItemSelectedListener groupSpinnerListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parentView, View view, int pos, long id) 
		{
			if(groupSpinnerInitialized)
			{
				//??mapper = new EpviMeterListMapper(context);
//				elvAdapter.setMapper(mapper, pos);
//				elvAdapter.notifyDataSetChanged();
			}
			else
				groupSpinnerInitialized = true;
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{}
	};
	
	private OnItemSelectedListener filterSpinnerListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parentView, View view, int pos, long id) 
		{
			if(filterSpinnerInitialized)
			{
				((EditText) getView().findViewById(R.id.filterListTB)).requestFocusFromTouch();
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				if(imm != null)
					imm.showSoftInput(((EditText) getView().findViewById(R.id.filterListTB)), InputMethodManager.SHOW_IMPLICIT);
			}
			else
				filterSpinnerInitialized = true;
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{}
	};    
    
    private OnGroupClickListener elvGroupClickListener = new OnGroupClickListener()
    {
    	@Override
        public boolean onGroupClick(ExpandableListView parent, View v, int group, long id) 
    	{
    		if(parent.isGroupExpanded(group))
    			parent.collapseGroup(group);
    		else
    			parent.expandGroup(group);
            return true;
        }    	
    };
    
    private OnClickListener elvOtionsViewListener = new OnClickListener() {
        public void onClick(View v) 
        {    
        	((View) getView().findViewById(R.id.epviListOptionsBT)).setVisibility(View.GONE);
        	((View) getView().findViewById(R.id.epviListOptions1LL)).setVisibility(View.VISIBLE);
        	((View) getView().findViewById(R.id.epviListOptions2LL)).setVisibility(View.VISIBLE);
        	((View) getView().findViewById(R.id.epviListOptions3LL)).setVisibility(View.VISIBLE);
        }
    };
    
    private OnClickListener elvShowViewListener = new OnClickListener() {
        public void onClick(View v) 
        {    
        	((View) getView().findViewById(R.id.epviListOptionsBT)).setVisibility(View.VISIBLE);
        	((View) getView().findViewById(R.id.epviListOptions1LL)).setVisibility(View.GONE);
        	((View) getView().findViewById(R.id.epviListOptions2LL)).setVisibility(View.GONE);
        	((View) getView().findViewById(R.id.epviListOptions3LL)).setVisibility(View.GONE);

        	int groupPos = ((Spinner) getView().findViewById(R.id.groupByDD)).getSelectedItemPosition();
        	int filterPos = ((Spinner) getView().findViewById(R.id.filterDD)).getSelectedItemPosition();
        	elvAdapter.setMapper(mapper, groupPos);			
        	mapper.SetNewFilter(filterPos, ((TextView) getView().findViewById(R.id.filterListTB)).getText().toString());
        	setFilterSummaryLA();
			elvAdapter.notifyDataSetChanged();
			
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if(imm != null){
				imm.hideSoftInputFromWindow(((TextView) getView().findViewById(R.id.filterListTB)).getWindowToken(), 0);
}
        }
    };
    
    private OnClickListener elvResetViewListener = new OnClickListener() {
        public void onClick(View v) 
        {    
        	((View) getView().findViewById(R.id.epviListOptionsBT)).setVisibility(View.VISIBLE);
        	((View) getView().findViewById(R.id.epviListOptions1LL)).setVisibility(View.GONE);
        	((View) getView().findViewById(R.id.epviListOptions2LL)).setVisibility(View.GONE);
        	((View) getView().findViewById(R.id.epviListOptions3LL)).setVisibility(View.GONE);
			((EditText) getView().findViewById(R.id.filterListTB)).setText("");
			mapper.ResetFilter();
			((Spinner) getView().findViewById(R.id.filterDD)).setSelection(0);
			((Spinner) getView().findViewById(R.id.groupByDD)).setSelection(0);
        	setFilterSummaryLA();
			mapper.resetChildViewExpanded();
        	elvAdapter.setMapper(mapper, 0);
			elvAdapter.notifyDataSetChanged();
        }
    };
    
    public void RedoMappings()
    {
    	mapper.DoDataMapping(((Spinner) getView().findViewById(R.id.groupByDD)).getSelectedItemId());
		elvAdapter.notifyDataSetChanged();
    }

	public void setFilterSummaryLA() {
		StringBuilder sb = new StringBuilder();
    	sb.append(Statics.getDefaultResources().getString(R.string.group_prompt));
    	sb.append(" ");
    	sb.append(((Spinner) getView().findViewById(R.id.groupByDD)).getSelectedItem().toString());
    	sb.append(", ");
    	sb.append(Statics.getDefaultResources().getString(R.string.filter_prompt));
    	sb.append(" ");
    	Object selectedFilterItem = ((Spinner) getView().findViewById(R.id.filterDD)).getSelectedItem();
    	sb.append(selectedFilterItem.toString());
    	((TextView) getView().findViewById(R.id.epviListFilterSummaryLA)).setText(sb.toString());
	}
	
    public OnEditorActionListener epviListTBListener = new OnEditorActionListener()
    {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
		{
			 if (actionId == EditorInfo.IME_ACTION_DONE) {
				 elvShowViewListener.onClick(null);

				 return true;
			 }
				 return false;
		}
    	
    };
        
    private OnChildClickListener elvChildClickListener = new OnChildClickListener() 
    {	 
		@Override
        public boolean onChildClick(ExpandableListView parent, View v, int group, int child, long id) 
        {
			final EditText input = new EditText(context);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			View c = elvAdapter.getChildView(group, child, false, null, null);
			Gauge st = elvAdapter.GetGaugeFormViewId(c.getId());
			Reading rd = elvAdapter.GetReadingFromViewId(c.getId());
			context.openGaugeDisplayDialog(st, rd);
            return true;
        }
    };
    
    private OnItemLongClickListener elvLongClickListener = new OnItemLongClickListener() 
    {	 
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
			if(v.getTag() != null && v.getTag().toString().equals(Statics.EPVI_LIST_ITEM))
			{
				elvAdapter.SetExpandedStateFormViewId(v.getId(), true);
			}
			else if(v.getTag() != null && v.getTag().toString().equals(Statics.EPVI_LIST_ITEM_EXPANDED))
			{
				elvAdapter.SetExpandedStateFormViewId(v.getId(), false);
			}
			elvAdapter.notifyDataSetChanged();
			return true;
		}
    };

	public MainActivity getContext() {
		return context;
	}

	public void setContext(MainActivity context) {
		this.context = context;
	}
}
