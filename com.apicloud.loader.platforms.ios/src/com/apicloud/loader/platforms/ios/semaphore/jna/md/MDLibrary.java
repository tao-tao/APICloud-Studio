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

package com.apicloud.loader.platforms.ios.semaphore.jna.md;
 

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFDictionary;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFString;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFType;
 
/**
 * @author semaphore
 * @author Alexander Eliseyev
 */
 public interface MDLibrary extends Library {

   public static final MDLibrary INSTANCE =LibraryFinder.getMDLibrary();
 
   int AMDeviceStartService(am_device device, CFString service_name, afc_connection handle, int unknown);

   int AFCConnectionOpen(CFType handle, int io_timeout, afc_connection.ByReference conn[]);

   int AFCDirectoryOpen(afc_connection conn, byte[] path, afc_directory.ByReference dir[]);

   int AFCDirectoryRead(afc_connection conn, afc_directory.ByReference dir, afc_directorys.ByReference dirent[]);

   int AMDeviceStartHouseArrestService(am_device.ByReference device, CFString identifier, CFDictionary unknown, afc_connection.ByReference handle, int what);

   int AFCFileRefOpen(afc_connection.ByReference conn, byte[] path, long mode, afc_file_ref.ByReference ref);
   
   int AFCFileRefWrite(afc_connection.ByReference conn, long ref, byte[] buf, int len);
   
   int AFCFileRefClose(afc_connection.ByReference conn, long ref);
   
   public static final int ADNCI_MSG_CONNECTED = 1;
   public static final int ADNCI_MSG_DISCONNECTED = 2;
   public static final int ADNCI_MSG_UNKNOWN = 3;
   
   int AMDeviceNotificationSubscribe(am_device_notification_callback callback, int unused0, int unused1, int unused2, am_device_notification.ByReference notification[]);
 
   int AMRestoreRegisterForDeviceNotifications(am_restore_device_notification_callback dfu_connect_callback, 
                                               am_restore_device_notification_callback recovery_connect_callback, 
											   am_restore_device_notification_callback dfu_disconnect_callback, 
											   am_restore_device_notification_callback recovery_disconnect_callback, 
											   int unknown, 
											   Pointer userInfo);
 
   int AMDeviceConnect(am_device device);
 
   int AMDeviceIsPaired(am_device device);
 
   int AMDeviceValidatePairing(am_device device);
 
   int AMDeviceStartSession(am_device device);
 
   int AMDeviceRelease(am_device device);
 
   CFType AMDeviceCopyValue(am_device device, int mbz, CFString key);
 
   int AMDeviceEnterRecovery(am_device device);

   int AMRecoveryModeDeviceSetAutoBoot(am_recovery_device rdev, byte autoBoot);
 
   CFString AMRecoveryModeDeviceCopySerialNumber(am_recovery_device rdev);
 
   int AMRecoveryModeDeviceReboot(am_recovery_device rdev);
 
  /// <i>native declaration : mobiledevice.h</i>
   
 public static class am_device_notification extends Structure {
     /// 0
     public int unknown0;
     /// 4
     public int unknown1;
     /// 8
     public int unknown2;
    /**
     * 12<br>
     * C type : am_device_notification_callback
     */
     public am_device_notification_callback callback;
     /// 16
     public int unknown3;
 
     public am_device_notification() {
         super();
     }
 
    /**
     * @param unknown0 0<br>
     * @param unknown1 4<br>
     * @param unknown2 8<br>
     * @param callback 12<br>
     *                 C type : am_device_notification_callback<br>
     * @param unknown3 16
     */
     public am_device_notification(int unknown0, int unknown1, int unknown2, am_device_notification_callback callback, int unknown3) {
       super();
       this.unknown0 = unknown0;
       this.unknown1 = unknown1;
       this.unknown2 = unknown2;
       this.callback = callback;
       this.unknown3 = unknown3;
     }
 
     protected ByReference newByReference() {
       return new ByReference();
     }
 
     protected ByValue newByValue() {
       return new ByValue();
     }
 
     protected am_device_notification newInstance() {
       return new am_device_notification();
     }
 
     public static class ByReference extends am_device_notification implements Structure.ByReference {
     }
 
     public static class ByValue extends am_device_notification implements Structure.ByValue {
     }
     
     /*@Override
     protected List getFieldOrder() {
       return FieldUtil.getFieldsOrderForClass(getClass());
     }*/
   }
   
   public interface am_device_notification_callback extends Callback {
     void invoke(am_device_notification_callback_info info, Pointer arg);
   }
   
   public interface am_restore_device_notification_callback extends Callback {
     void invoke(am_recovery_device rdev);
   }
   
   public static class am_device_notification_callback_info extends Structure {
    /**
     * 0    device<br>
     * C type : am_device*
     */
     public am_device.ByReference dev;
    /// 4    one of ADNCI_MSG_*
     public int msg;
 
     public am_device_notification_callback_info() {
       super();
     }
 
    /**
     * @param dev 0    device<br>
     *            C type : am_device*<br>
     * @param msg 4    one of ADNCI_MSG_*
     */
     public am_device_notification_callback_info(am_device.ByReference dev, int msg) {
       super();
       this.dev = dev;
       this.msg = msg;
     }
 
     protected ByReference newByReference() {
       return new ByReference();
     }
 
     protected ByValue newByValue() {
       return new ByValue();
     }
 
     protected am_device_notification_callback_info newInstance() {
       return new am_device_notification_callback_info();
     }
 
     public static class ByReference extends am_device_notification_callback_info implements Structure.ByReference {

     }
 
     public static class ByValue extends am_device_notification_callback_info implements Structure.ByValue {

     }
     
      /*@Override
      protected List getFieldOrder() {
        return FieldUtil.getFieldsOrderForClass(getClass());
      }*/
   }
  /// <i>native declaration : mob
   
   public static class am_device extends Structure {
     public byte[] unknown0 = new byte[(16)];
     public int device_id;
     public int product_id;
     public Pointer serial;
     public int unknown1;
     public byte[] unknown2 = new byte[(4)];
     public int lockdown_conn;
     public byte[] unknown3 = new byte[(8)];

     public am_device() {
       super();
     }

     protected ByReference newByReference() {
       return new ByReference();
     }
 
     protected ByValue newByValue() {
       return new ByValue();
     }
 
     protected am_device newInstance() {
       return new am_device();
     }

     public static class ByReference extends am_device implements Structure.ByReference {

     }
 
     public static class ByValue extends am_device implements Structure.ByValue {

     }
     /*@Override
     protected List getFieldOrder() {
       return FieldUtil.getFieldsOrderForClass(getClass());
     }*/
     public CFType getValue(String key) {
       if (key == null)
           return INSTANCE.AMDeviceCopyValue(this, 0, null);
       return INSTANCE.AMDeviceCopyValue(this, 0, CFString.buildString(key));
     }
 
     public boolean enterRecovery() {
       return INSTANCE.AMDeviceEnterRecovery(this) == 0;
     }
   }
   
   public static class am_recovery_device extends Structure {
    public byte[] unknown0 = new byte[(8)]; /// 0
    public am_restore_device_notification_callback callback; /// 8
    public Pointer user_info; /// 12
    public byte[] unknown1 = new byte[(12)]; /// 16
    public int readwrite_pipe; /// 28
    public byte read_pipe; /// 32
    public byte write_ctrl_pipe; /// 33
    public byte read_unknown_pipe; /// 34
    public byte write_file_pipe; /// 35
    public byte write_input_pipe; /// 36

    public am_recovery_device() {
      super();
    }

     protected ByReference newByReference() {
       ByReference s = new ByReference();
       s.useMemory(getPointer());
       write();
       s.read();
       return s;
     }
 
     protected ByValue newByValue() {
       ByValue s = new ByValue();
       s.useMemory(getPointer());
       write();
       s.read();
       return s;
     }
 
     protected am_recovery_device newInstance() {
       am_recovery_device s = new am_recovery_device();
       s.useMemory(getPointer());
       write();
       s.read();
       return s;
     }

    public static class ByReference extends am_recovery_device implements Structure.ByReference {
    }

    public static class ByValue extends am_recovery_device implements Structure.ByValue {
    }

     public int exitRecovery() {
       if (setAutoBoot(true) == 0) {
         reboot();
         return 0;
       }
       return -1;
     }
 
     public void reboot() {
       INSTANCE.AMRecoveryModeDeviceReboot(this);
     }
 
     public int setAutoBoot(boolean value) {
       return INSTANCE.AMRecoveryModeDeviceSetAutoBoot(this, value ? (byte) 1 : (byte) 0);
     }
 
     /*@Override
      protected List getFieldOrder() {
        return FieldUtil.getFieldsOrderForClass(getClass());
      }*/
   }   
   int AMDeviceDisconnect(am_device.ByReference device);
   
   int AMDeviceLookupApplications(am_device device, int options, CFDictionaryRef.ByReference result);
   
   int AMDeviceUninstallApplication(CFType socket, CFString path, CFDictionary options, install_callback callback, int user);

   int AMDeviceTransferApplication(CFType socket, CFString path, int options, transfer_callback callback, int user);
 
   int AMDeviceInstallApplication(CFType socket, CFString path, CFDictionary options, install_callback callback, int user);
 
   int AFCDirectoryCreate(afc_connection.ByReference conn, byte[] dirname);
 
   public static class CFDictionaryRef extends Structure {
	   public CFType key;
	   public CFType value;
 
	   public CFDictionaryRef() {
	   }
 
	   public CFDictionaryRef(CFType key, CFType value) {
		   this.key = key;
		   this.value = value;
	   }
 
	   protected ByReference newByReference() {
		   ByReference s = new ByReference();
		   s.useMemory(getPointer());
		   write();
		   s.read();
		   return s;
	   }
 
	   protected ByValue newByValue() {
		   ByValue s = new ByValue();
		   s.useMemory(getPointer());
		   write();
		   s.read();
		   return s;
	   }
 
	   protected CFDictionaryRef newInstance() {
		   return new CFDictionaryRef();
	   }
 
	   public static class ByReference extends MDLibrary.CFDictionaryRef implements Structure.ByReference {
	   }
 
	   public static class ByValue extends MDLibrary.CFDictionaryRef implements Structure.ByValue {
	   }
   	}
 
   	public static class afc_connection extends Structure {
   		public CFType handle;
 
   		public afc_connection() {
   		}
 
   		public afc_connection(CFType handle) {
   			this.handle = handle; 
   		}
 
   		protected ByValue newByValue() {
   			return new ByValue();
   		}
 
   		protected afc_connection newInstance() {
   			return new afc_connection();
   		}
   		
   		protected ByReference newByReference() {
   			return new ByReference(); 
   		}
 
   		public static class ByReference extends afc_connection implements Structure.ByReference {
   		}
 
   		public static class ByValue extends afc_connection implements Structure.ByValue {
   		}
   	}
 
   	public static class afc_directory extends Structure {
   		public CFType _cnt;
 
   		public afc_directory() {
   		}
 
   		public afc_directory(CFType _cnt) {
   			this._cnt = _cnt; 
   		}
 
   		protected ByReference newByReference() {
   			ByReference s = new ByReference();
   			s.useMemory(getPointer());
   			write();
   			s.read();
   			return s;
   		}
 
   		protected ByValue newByValue() {
   			ByValue s = new ByValue();
   			s.useMemory(getPointer());
   			write();
   			s.read();
   			return s;
   		}
 
   		protected afc_directory newInstance() {
   			return new afc_directory(); 
   		}
 
   		public static class ByReference extends afc_directory implements Structure.ByReference { 
   		}
 
   		public static class ByValue extends afc_directory implements Structure.ByValue {  
   		} 
   	}
 
    public static class afc_directorys extends Structure { 
    	public byte[] paths = new byte[256];
 
    	public afc_directorys() {
    	}
 
    	public afc_directorys(byte[] paths) {
    		this.paths = paths; 
    	}
 
    	protected ByReference newByReference() {
    		ByReference s = new ByReference();
    		s.useMemory(getPointer());
    		write();
    		s.read();
    		return s;
    	}
 
    	protected ByValue newByValue() {
    		ByValue s = new ByValue();
    		s.useMemory(getPointer());
    		write();
    		s.read();
    		return s;
    	}
 
    	protected afc_directorys newInstance() {
    		return new afc_directorys(); 
    	}
 
    	public static class ByReference extends afc_directorys implements Structure.ByReference { }
 
    	public static class ByValue extends afc_directorys implements Structure.ByValue { } 
    }
 
    public static class afc_file_ref extends Structure {
     	public long _cnt;
 
     	public afc_file_ref() { 
     	}
 
     	public afc_file_ref(long _cnt) { this._cnt = _cnt; }
 
     	protected ByReference newByReference() {
     		return new ByReference(); 
     	}
 
     	protected ByValue newByValue() {
     		return new ByValue();
     	}
 
     	protected afc_file_ref newInstance() {
     		return new afc_file_ref();
     	}
 
     	public static class ByReference extends afc_file_ref implements Structure.ByReference {
     	}
 
     	public static class ByValue extends afc_file_ref implements Structure.ByValue {
     	}
    }
 
    public static abstract interface install_callback extends Callback {
    	void invoke(CFDictionary dict, int arg);
    }
 
    public static abstract interface transfer_callback extends Callback {
    	void invoke(CFDictionary dict, int arg);
    }
 }
