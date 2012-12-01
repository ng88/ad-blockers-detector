/*
 * Tools.java
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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class Tools
{

    /**
     * Quit the current application.
     */
    public static final void quitApplication()
    {
        System.runFinalizersOnExit(true);
        System.exit(0);
    }
    
    /**
     * Propose user to send an email with pre-filled fields.
     */
    public static final void sendEMail(final Context context, final String dialogTitle, final String to, final String subject, final String body)
    {
        final Intent send = new Intent(Intent.ACTION_SENDTO);
        final String uriText =
                "mailto:" + Uri.encode(to) + 
                "?subject=" + Uri.encode(subject) + 
                "&body=" + Uri.encode(body);
        send.setData(Uri.parse(uriText));
        context.startActivity(Intent.createChooser(send, dialogTitle));
    }
    
    private Tools() {}
    
}
