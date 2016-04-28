package com.huiwu.model.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huiwu.model.R;

/**
 * Created by HuiWu on 2015/10/9.
 */
public class MyAlertDialog extends Dialog {
	private static MyAlertDialog dialog = null;

	public MyAlertDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	public void setCancelable(boolean flag) {
		super.setCancelable(flag);
	}

	public static class Builder {
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String neutralButtonText;
		private String negativeButtonText;
		private String[] items;
		private View contentView;

		private OnClickListener
				positiveButtonClickListener,
				negativeButtonClickListener,
				neutralButtonClickListener,
				itemsOnClickListener;

		public Builder(Context context) {
			this.context = context;
			dialog = new MyAlertDialog(context, R.style.custom_dialog_style);
		}

		public void setCancelable(boolean flag) {
			dialog.setCancelable(flag);
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set the Dialog items from resource
		 *
		 * @param items
		 * @return
		 */
		public Builder setItems(int items, OnClickListener listener) {
			this.items = context.getResources().getStringArray(items);
			this.itemsOnClickListener = listener;
			return this;
		}

		/**
		 * Set the Dialog items from String
		 *
		 * @param items
		 * @return
		 */
		public Builder setItems(String[] items, OnClickListener listener) {
			this.items = items;
			this.itemsOnClickListener = listener;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog.
		 * If a message is set, the contentView is not
		 * added to the Dialog...
		 *
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		public Builder setPositiveButton(int positiveButtonText, OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}


		public Builder setPositiveButton(String positiveButtonText, OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(int negativeButtonText,
		                                 OnClickListener listener) {
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(String negativeButtonText,
		                                 OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNeutralButton(int neutralButtonText,
		                                OnClickListener listener) {
			this.neutralButtonText = (String) context
					.getText(neutralButtonText);
			this.neutralButtonClickListener = listener;
			return this;
		}

		public Builder setNeutralButton(String neutralButtonText,
		                                OnClickListener listener) {
			this.neutralButtonText = neutralButtonText;
			this.neutralButtonClickListener = listener;
			return this;
		}

		public MyAlertDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.layout_my_alert_dialog, null);
			dialog.addContentView(layout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
			TextView textTitle = (TextView) layout.findViewById(R.id.title);
			if (title != null) {
				textTitle.setText(title);
			} else {
				textTitle.setVisibility(View.GONE);
			}
			final ListView list_items = (ListView) layout.findViewById(R.id.items);
			if (items != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);
				list_items.setAdapter(adapter);
				if (itemsOnClickListener != null)
					list_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
							itemsOnClickListener.onClick(dialog, position);
							dialog.dismiss();
						}
					});
			} else {
				list_items.setVisibility(View.GONE);
			}
			Button positiveButton = (Button) layout.findViewById(R.id.positiveButton);
			if (positiveButtonText != null) {
				positiveButton.setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					positiveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			} else {
				positiveButton.setVisibility(View.GONE);
			}
			Button neutralButton = (Button) layout.findViewById(R.id.neutralButton);
			if (neutralButtonText != null) {
				neutralButton.setText(neutralButtonText);
				if (neutralButtonClickListener != null) {
					(layout.findViewById(R.id.neutralButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									neutralButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_NEUTRAL);
								}
							});
				}
			} else {
				neutralButton.setVisibility(View.GONE);
			}
			Button negativeButton = (Button) layout.findViewById(R.id.negativeButton);
			if (negativeButtonText != null) {
				negativeButton.setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					(layout.findViewById(R.id.negativeButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									negativeButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			} else {
				negativeButton.setVisibility(View.GONE);
			}
			if (message != null) {
				((TextView) layout.findViewById(R.id.message)).setText(message);
			} else if (contentView != null) {
				((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content)).addView(contentView);
			}
			dialog.setContentView(layout);
			return dialog;
		}

		public MyAlertDialog show() {
			MyAlertDialog dialog = create();
			dialog.show();
			return dialog;
		}
	}

}
