/*
 * AdBlockersDetector.java
 * 
 * Copyright (c) 2012, Nicolas GUILLAUME. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package fr.nghs.android.abd;

import java.net.URLEncoder;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;

public class DialogBuilder
{
	
	public static final int BUTTON_QUIT = AlertDialog.BUTTON_NEGATIVE;
	public static final int BUTTON_BUY = AlertDialog.BUTTON_POSITIVE;
	public static final int BUTTON_CONTACT_US = AlertDialog.BUTTON_NEUTRAL;
	
	private AlertDialog.Builder b;
	private boolean canBuyAdFreeVersion = false;
	private boolean customTextSet = false;
	private Activity act;
	private DefaultButtonHandlers defaultHandler = new DefaultButtonHandlers();
	
	public DialogBuilder(Activity c)
	{
		act = c;
		b = new AlertDialog.Builder(c);
		b.setCancelable(false);
		b.setTitle(R.string.dlg_title);
		
		b.setOnCancelListener(defaultHandler);
		setBuyButton(defaultHandler);
		setQuitButton(defaultHandler);
		setContactButton(defaultHandler);
	}

	public Dialog create()
	{
		// set default text
		if(!customTextSet)
		{
			String txt = act.getString(R.string.dlg_default_text);
			if(canBuyAdFreeVersion)
				txt += act.getString(R.string.dlg_default_text_buy);
			b.setMessage(Html.fromHtml(txt));
		}
		return b.create();
	}

	public DialogBuilder setCustomTitle(View customTitleView)
	{
		b.setCustomTitle(customTitleView);
		return this;
	}

	public DialogBuilder setIcon(Drawable icon)
	{
		b.setIcon(icon);
		return this;
	}

	public DialogBuilder setIcon(int iconId)
	{
		b.setIcon(iconId);
		return this;
	}

	public DialogBuilder setMessage(CharSequence message)
	{
		customTextSet = (message != null);
		b.setMessage(message);
		return this;
	}

	public DialogBuilder setMessage(int messageId)
	{
		customTextSet = (messageId != 0);
		b.setMessage(messageId);
		return this;
	}

	public DialogBuilder setTitle(CharSequence title)
	{
		b.setTitle(title);
		return this;
	}

	public DialogBuilder setTitle(int titleId)
	{
		b.setTitle(titleId);
		return this;
	}

	public DialogBuilder setView(View view)
	{
		customTextSet = (view != null);
		b.setView(view);
		return this;
	}

	public Dialog show()
	{
		final Dialog d = create();
		d.show();
		return d;
	}
	
	public DialogBuilder setContactButton(String emailAddress)
	{
		defaultHandler.setEmailAddress(emailAddress);
		return setContactButton(defaultHandler);
	}
	
	public DialogBuilder setContactButton(int textRes, String emailAddress)
	{
		defaultHandler.setEmailAddress(emailAddress);
		return setContactButton(textRes, defaultHandler);
	}

	public DialogBuilder setContactButton(CharSequence text, String emailAddress)
	{
		defaultHandler.setEmailAddress(emailAddress);
		return setContactButton(text, defaultHandler);
	}
	
	public DialogBuilder setContactButton(OnClickListener listener)
	{
		return setContactButton(R.string.contact_us, listener);
	}
	
	public DialogBuilder setContactButton(int textRes, OnClickListener listener)
	{
		return setContactButton(act.getString(textRes), listener);
	}

	public DialogBuilder setContactButton(CharSequence text, OnClickListener listener)
	{
		b.setNeutralButton(text, listener);
		return this;
	}
	
	public DialogBuilder setQuitButton(OnClickListener listener)
	{
		return setQuitButton(R.string.quit, listener);
	}
	
	public DialogBuilder setQuitButton(int textRes, OnClickListener listener)
	{
		return setQuitButton(act.getString(textRes), listener);
	}

	public DialogBuilder setQuitButton(CharSequence text, OnClickListener listener)
	{
		b.setNegativeButton(text, listener);
		return this;
	}

	public DialogBuilder setBuyButton(OnClickListener listener)
	{
		return setBuyButton(R.string.buy, listener);
	}
	
	public DialogBuilder setBuyButton(int textRes, OnClickListener listener)
	{
		return setBuyButton(act.getString(textRes), listener);
	}
	
	public DialogBuilder setBuyButton(CharSequence text, OnClickListener listener)
	{
		canBuyAdFreeVersion = (listener != null);
		b.setPositiveButton(text, listener);
		return this;
	}
	
	public final void setAdBlockerInfo(AdBlockersDetector.Info info)
	{
		defaultHandler.setInfo(info);
	}
	
	private final class DefaultButtonHandlers implements OnClickListener, OnCancelListener
	{
		private String emailAddress = null;
		private AdBlockersDetector.Info info = null;
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			switch(which)
			{
			case BUTTON_CONTACT_US:
				if(emailAddress != null)
				{
					String infoStr = "";
					if(info != null)
						infoStr = " (" + info.method.ordinal() + ")";
					Intent send = new Intent(Intent.ACTION_SENDTO);
					String uriText;

					uriText = "mailto:" + URLEncoder.encode(emailAddress) + 
							"?subject=" + URLEncoder.encode(act.getString(R.string.dlg_title)) + 
							"&body=" + URLEncoder.encode(act.getString(R.string.contact_default_msg) + infoStr);
					send.setData(Uri.parse(uriText));
					act.startActivity(Intent.createChooser(send, act.getString(R.string.contact_us)));
					break;
				}
			case BUTTON_BUY:
				Log.w("ABD", "No listener defined.");
				break;
			case BUTTON_QUIT:
				quit();
				break;
			}
		}

		@Override
		public void onCancel(DialogInterface dialog)
		{
			quit();
		}

		public final void setEmailAddress(String emailAddress)
		{
			this.emailAddress = emailAddress;
		}

		public final void setInfo(AdBlockersDetector.Info info)
		{
			this.info = info;
		}
	}
	
	private static final void quit()
	{
		System.runFinalizersOnExit(true);
		System.exit(0);
	}
	
}
