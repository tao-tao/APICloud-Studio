/* 
 * TinyUmbrella - making iDevice restores possible... 
 * Copyright (C) 2009-2010 semaphore 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
  * This program is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
  * GNU General Public License for more details. 
  * 
  * You should have received a copy of the GNU General Public License 
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */ 

package com.apicloud.loader.platforms.ios.semaphore.jna.img3;
 
 import java.nio.ByteBuffer;
 
/**
 * Brought to you by:
 * User: semaphore
 * Date: May 27, 2010
 * Time: 8:35:17 PM
 */
public class Img3File {
   public ByteBuffer data = null;
   public Img3Header header = null;
   public Img3Element type_element = null;
   public Img3Element data_element = null;
   public Img3Element vers_element = null;
   public Img3Element sepo_element = null;
   public Img3Element bord_element = null;
   public Img3Element kbag1_element = null;
   public Img3Element kbag2_element = null;
   public Img3Element ecid_element = null;
   public Img3Element shsh_element = null;
   public Img3Element cert_element = null;
 }
