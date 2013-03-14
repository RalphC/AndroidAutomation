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
	private int minX;
	private int minY;
	private double scalex;
	private double scaley;
	private ViewNode result;
	
	public TreeView(int Width, int Height, double scalex, double scaley) {
	    HierarchyViewerDirector.getDirector().loadViewHierarchy();
	    this.minX = Width;
	    this.minY = Height;
	    this.scalex = scalex;
	    this.scaley = scaley;
	    this.result = null;
	}
	
	public ViewNode SearchId(MouseEvent event) {
		int posX = (int)(event.getX() * scalex);
		int posY = (int)(event.getY() * scaley);
		IDevice device = DeviceSelectionModel.getModel().getSelectedDevice();
		ViewNode rootNode = loadWindowData(Window.getFocusedWindow(device));
		findViewByPosition(posX, posY, rootNode);
		return result;
	}
	
	private boolean isInside(ViewNode node, int PosX, int PosY) {
		PosX = PosX - node.left;
		if (PosX < 0 ) return false;
		PosY = PosY - node.top;
		if (PosY < 0) return false;
		return (PosX < node.width) && (PosY < node.height);
	}
	
	
	private void findViewByPosition(int PosX, int PosY, ViewNode rootNode) {
		if (!isInside(rootNode, PosX, PosY)) {
			return;
		}
		if (rootNode.children.isEmpty()) {
			if (rootNode.width < minX && rootNode.height < minY) {
				result = rootNode;
			}
			return;
		}
		for (ViewNode node : rootNode.children) {
			findViewByPosition(PosX, PosY, node);
		}
		return;
	}
	
    private ViewNode loadWindowData(Window window) {
        DeviceConnection connection = null;
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
            }
            if (currentNode == null) {
                return null;
            }
            while (currentNode.parent != null) {
                currentNode = currentNode.parent;
            }
            return currentNode;
        } catch (Exception e) {
            Log.e("TreeViewer", "Unable to load window data for window " + window.getTitle() + " on device "
                    + window.getDevice());
            Log.e("TreeViewer", e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }
	
}
