package com.android.monkeyrunner.recorder;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.android.hierarchyviewerlib.HierarchyViewerDirector;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.DeviceConnection;
import com.android.hierarchyviewerlib.device.ViewNode;
import com.android.hierarchyviewerlib.device.Window;
import com.android.hierarchyviewerlib.device.DeviceBridge.ViewServerInfo;
import com.android.hierarchyviewerlib.models.DeviceSelectionModel;

public class TreeView {

	public TreeView() {
	    HierarchyViewerDirector.getDirector().loadViewHierarchy();
	}
	
	public String getId(MouseEvent event) {
		int posX = event.getX();
		int posY = event.getY();
		return SearchId(posX, posY);
		
	}
	
	private String SearchId(int PosX, int PosY) {
		int minY = 1024;
		int minX = 1024;
		String CurrentId = "";
		
		IDevice device = DeviceSelectionModel.getModel().getSelectedDevice();
		
		HashMap ViewNodeMap = loadWindowData(Window.getFocusedWindow(device));
		Iterator it = ViewNodeMap.keySet().iterator();
		while(it.hasNext()){
			ViewNode node = (ViewNode) it.next();
			if(node.top < PosY && node.left < PosX){
				if((node.top + node.height) > PosY && (node.left + node.width) > PosX) {
					if((PosY - node.top) < minY && (PosX - node.left) < minX) {
						minY = PosY - node.top;
						minX = PosX - node.left;
						CurrentId = node.id;
					}
				}
			}
		}
		
		return CurrentId;
		
	}
	
	
    private HashMap loadWindowData(Window window) {
        DeviceConnection connection = null;
        HashMap ViewNodeMap = new HashMap();
        try {
            connection = new DeviceConnection(window.getDevice());
            connection.sendCommand("DUMP " + window.encode()); //$NON-NLS-1$
            BufferedReader in = connection.getInputStream();
            ViewNode currentNode = null;
            
            int currentDepth = -1;
            String line;
            while ((line = in.readLine()) != null) {
                if ("DONE.".equalsIgnoreCase(line)) {
                    break;
                }
                int depth = 0;
                while (line.charAt(depth) == ' ') {
                    depth++;
                }
                while (depth <= currentDepth) {
                    currentNode = currentNode.parent;
                    currentDepth--;
                }
                currentNode = new ViewNode(window, currentNode, line.substring(depth));
                currentDepth = depth;
                ViewNodeMap.put(currentNode, currentNode.id);
                
            }
        } catch (Exception e) {
            Log.e("TreeViewer", "Unable to load window data for window " + window.getTitle() + " on device "
                    + window.getDevice());
            Log.e("TreeViewer", e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return ViewNodeMap;
    }
	
}
