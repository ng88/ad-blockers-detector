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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

/**
 * This class provides a way to detect ad blockers.
 * @author ng
 *
 */
public class AdBlockersDetector
{

	public static interface Callback
	{
		/**
		 * Called in the GUI thread when result is available.
		 * @param adBlockFound true if an ad blocker is installed.
		 */
		public void onResult(boolean adBlockerFound);
	}

	public static enum Method
	{
		/** Not found */
		NONE,
		/** Detected by reading host file */
		BY_HOSTS_FILE,
		/** Detected by installed app names */
		BY_APP_NAME,
		/** Detected by resolving host names */
		BY_HOST_RESOLUTION;
	}
	
	/** Give information on how ad blocker was detected */
	public static class Info
	{
		/** The used method */
		Method method;
		/** Details, depending on method */
		String details;
	}


	private WeakReference<Context> context;
	
	/**
	 * @param c c can be null, in this case the method using package name is not used.
	 */
	public AdBlockersDetector(Context c)
	{
		context = new WeakReference<Context>(c);
	}
	
	/**
	 * Asynchronous ad-blockers detection.
	 * Callback is called in GUI thread.
	 * @param info if not null, it will be filled.
	 * @param callback
	 */
	public void detectAdBlockers(Info info, Callback callback)
	{
		new DetectTask(callback, info).execute();
	}

	/**
	 * Asynchronous ad-blockers detection.
	 * Callback is called in GUI thread.
	 * @param callback
	 */
	public void detectAdBlockers(Callback callback)
	{
		detectAdBlockers(null, callback);
	}
	
	/**
	 * Synchronous ad-blockers detection
	 * This is blocking and should be called in a separated thread.
	 * In Android activities, prefer the asynchronous version.
	 * @param info if not null, it will be filled.
	 * @return true if an ad-blocker is detected
	 */
	public boolean detectAdBlockers(Info info)
	{
		if(info != null)
		{
			info.method = Method.NONE;
			info.details = "";
		}
		return  detectAppNames(info) ||
				detectHostName(info) ||
				detectInHostFile(info);
	}
	
	/**
	 * Synchronous ad-blockers detection
	 * This is blocking and should be called in a separated thread.
	 * In Android activities, prefer the asynchronous version.
	 * @return true if an adblocker is detected
	 */
	public boolean detectAdBlockers()
	{
		return detectAdBlockers((Info)null);
	}
	
	private boolean detectInHostFile(Info info)
	{
		final File host = new File(HOST_FILE);
		if(host.canRead())
		{
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new FileReader(host));
				String ln;
				while( (ln = in.readLine()) != null )
				{
					if(ln.contains(HOST_AD_PATTERN))
					{
						if(info != null)
						{
							info.method = Method.BY_HOSTS_FILE;
							info.details = ln;
						}
						return true;
					}
				}
			}
			catch(Exception e)
			{
				return false;
			}	
			finally
			{
				try
				{
					if(in != null)
						in.close();
				}
				catch (IOException e) { }
			}
		}
		return false;
	}

	private boolean detectHostName(Info info)
	{
		for(final String h : HOSTS)
		{
			final String addr = isLocalHost(h);
			if(addr != null)
			{
				if(info != null)
				{
					info.method = Method.BY_HOST_RESOLUTION;
					info.details = h + " => " + addr;
				}
				return true;
			}
		}
		return false;
	}
	
	private String isLocalHost(String hostName)
	{
		try
		{
			final InetAddress a = InetAddress.getByName(hostName);
			if(a.isAnyLocalAddress() || a.isLinkLocalAddress() || a.isLoopbackAddress())
				return a.getHostAddress();
		}
		catch(Exception ex)
		{
		}
		return null;
	}
	
	private boolean detectAppNames(Info info)
	{
		if(context != null)
			for(final String app : BLOCKERS_APP_NAMES)
				if(isAppInstalled(app))
				{
					if(info != null)
					{
						info.method = Method.BY_APP_NAME;
						info.details = app;
					}
					return true;
				}
		return false;
	}
	
	private boolean isAppInstalled(String packageName)
	{
		try
		{
			final Context c = context.get();
			return (c != null) && (c.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES) != null);
		}
		catch(Exception e) // PackageManager.NameNotFoundException
		{
			return false;
		}
	}
	
	private static final String HOST_FILE = "/etc/hosts";

	private static final String HOST_AD_PATTERN = "admob";
	
	private static final String[] BLOCKERS_APP_NAMES = 
		{
		 "de.ub0r.android.adBlock",
		 "org.adblockplus.android",
		 "com.bigtincan.android.adfree",
		 "org.adaway"
		};
	
	private static final String[] HOSTS = 
		{
		 "a.admob.com",
		 "mm.admob.com",
		 "p.admob.com",
		 "r.admob.com",
		 "mmv.admob.com"
		};
	
	private class DetectTask extends AsyncTask<Void, Void, Boolean>
	{
		
		private WeakReference<Callback> callback;
		private Info info;
		
		public DetectTask(Callback c, Info inf)
		{
			callback = new WeakReference<Callback>(c);
			info = inf;
		}

		@Override
		protected Boolean doInBackground(Void... params)
		{
			return detectAdBlockers(info);
		}
		
		@Override
		protected void onPostExecute(Boolean r)
		{
			final Callback c = callback.get();
			if(c != null && r != null)
				c.onResult(r);
		}
		
	}
}
