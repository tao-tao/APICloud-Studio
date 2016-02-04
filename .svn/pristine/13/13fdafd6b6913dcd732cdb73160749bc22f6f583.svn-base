// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package org.cef.callback;

import java.util.Vector;

/**
 * The methods of this interface may be called
 * on any thread.
 */
public abstract interface CefDragData {
  /**
   * Returns true if the drag data is a link.
   */
	public abstract boolean isLink();

  /**
   * Returns true if the drag data is a text or html fragment.
   */
	public abstract boolean isFragment();

  /**
   * Returns true if the drag data is a file.
   */
	public abstract boolean isFile();

  /**
   * Return the link URL that is being dragged.
   */
	public abstract String getLinkURL();

  /**
   * Return the title associated with the link being dragged.
   */
	public abstract String getLinkTitle();

  /**
   * Return the metadata, if any, associated with the link being dragged.
   */
	public abstract String getLinkMetadata();

  /**
   * Return the plain text fragment that is being dragged.
   */
	public abstract String getFragmentText();

  /**
   * Return the text/html fragment that is being dragged.
   */
	public abstract String getFragmentHtml();

  /**
   * Return the base URL that the fragment came from. This value is used for
   * resolving relative URLs and may be empty.
   */
	public abstract String getFragmentBaseURL();

  /**
   * Return the name of the file being dragged out of the browser window.
   */
	public abstract String getFileName();

  /**
   * Retrieve the list of file names that are being dragged into the browser
   * window.
   */
	public abstract boolean getFileNames(Vector<String> names);
}