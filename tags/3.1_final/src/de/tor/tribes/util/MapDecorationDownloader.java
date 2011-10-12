/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.php.json.JSONArray;
import de.tor.tribes.php.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Jejkal
 */
public class MapDecorationDownloader {

    public static void main( String[] args ) throws Exception {
	byte[] worldData = new byte[1000000];
	for ( int i = 0; i < 1000000; i++ ) {
	    worldData[i] = 0;
	}
	Hashtable<Integer, Integer> mapMapping = new Hashtable<Integer, Integer>();
	mapMapping.put(0, WorldDecorationHolder.ID_GRAS1);
	mapMapping.put(1, WorldDecorationHolder.ID_GRAS2);
	mapMapping.put(2, WorldDecorationHolder.ID_GRAS3);
	mapMapping.put(3, WorldDecorationHolder.ID_GRAS4);
	mapMapping.put(28, WorldDecorationHolder.ID_ROCK1);
	mapMapping.put(29, WorldDecorationHolder.ID_ROCK2);
	mapMapping.put(30, WorldDecorationHolder.ID_ROCK3);
	mapMapping.put(31, WorldDecorationHolder.ID_ROCK4);
	mapMapping.put(32, WorldDecorationHolder.ID_FORREST1);
	mapMapping.put(33, WorldDecorationHolder.ID_FORREST2);
	mapMapping.put(34, WorldDecorationHolder.ID_FORREST3);
	mapMapping.put(35, WorldDecorationHolder.ID_FORREST4);
	mapMapping.put(36, WorldDecorationHolder.ID_FORREST5);
	mapMapping.put(37, WorldDecorationHolder.ID_FORREST6);
	mapMapping.put(38, WorldDecorationHolder.ID_FORREST7);
	mapMapping.put(39, WorldDecorationHolder.ID_FORREST8);
	mapMapping.put(40, WorldDecorationHolder.ID_FORREST9);
	mapMapping.put(41, WorldDecorationHolder.ID_FORREST10);
	mapMapping.put(42, WorldDecorationHolder.ID_FORREST11);
	mapMapping.put(43, WorldDecorationHolder.ID_FORREST12);
	mapMapping.put(44, WorldDecorationHolder.ID_FORREST13);
	mapMapping.put(45, WorldDecorationHolder.ID_FORREST14);
	mapMapping.put(46, WorldDecorationHolder.ID_FORREST15);
	mapMapping.put(47, WorldDecorationHolder.ID_FORREST16);
	mapMapping.put(48, WorldDecorationHolder.ID_SEA);

	Hashtable<String, JSONArray> sectorData = new Hashtable<String, JSONArray>();
	String world = "de68";
	for ( int x = 0; x < 1000; x++ ) {
	    for ( int y = 0; y < 1000; y++ ) {
		int xSector = x / 20;
		int ySector = y / 20;
		String secId = (xSector * 20) + "_" + (ySector * 20);

		JSONArray secData = sectorData.get(secId);
		if ( secData == null ) {
		    String url = "http://" + world + ".die-staemme.de/map/tiles/" + secId + ".json";
		    System.out.println("Read sector from '" + url + "'");
		    URLConnection ucon = new URL(url).openConnection();
		    byte[] data = new byte[1024];
		    StringBuilder b = new StringBuilder();
		    int cnt = 0;
		    while ( (cnt = ucon.getInputStream().read(data)) != -1 ) {
			b.append(new String(data, 0, cnt));
		    }
		    String jsonData = b.toString();
		    JSONObject sectorObject = new JSONObject(jsonData);
		    secData = (JSONArray) sectorObject.get("tiles");
		    sectorData.put(secId, secData);
		}

		int secX = x % 20;
		int secY = y % 20;
		JSONArray col = secData.getJSONArray(secX);
		int value = mapMapping.get(col.getInt(secY));
		worldData[y * 1000 + x] = (byte) value;

	    }
	}

	GZIPOutputStream gout = new GZIPOutputStream(new FileOutputStream(new File("world_" + world + ".dat.gz")));
	gout.write(worldData);
	gout.flush();
	gout.close();
	/*for ( int i = 0; i < 40; i += 20 ) {
	for ( int j = 0; j < 40; j += 20 ) {
	String url = "http://de43.die-staemme.de/map/tiles/" + i + "_" + j + ".json";
	URLConnection ucon = new URL(url).openConnection();
	byte[] data = new byte[2024];
	int bytesRead = ucon.getInputStream().read(data);
	String jsonData = new String(data, 0, bytesRead);
	JSONObject sectorData = new JSONObject(jsonData);
	JSONArray tiles = (JSONArray) sectorData.get("tiles");
	for ( int xs = 0; xs < 20; xs++ ) {
	JSONArray col = tiles.getJSONArray(xs);
	for ( int ys = 0; ys < 20; ys++ ) {
	int field = col.getInt(ys);
	int x = i * 20 + xs;
	int y = j * 20 + ys;

	}
	}
	}
	}


	WorldDecorationHolder.initialize();

	for ( int j = 0; j < 10; j++ ) {
	for ( int i = 0; i < 10; i++ ) {

	System.out.println(WorldDecorationHolder.getTextureId(i, j));
	}

	}
	 */

	/*JSONObject jo = new JSONObject("{'x':0,'y':0,'villages':[],'players':[],'allies':[],'tiles':[[0,3,1,3,33,0,2,2,3,3,1,0,0,32,1,1,2,48,33,2],[1,2,2,0,36,1,3,0,3,0,32,0,0,0,1,1,1,41,47,43],[0,0,0,1,3,0,0,1,32,0,0,1,3,0,1,3,1,45,46,46],[2,0,2,1,41,35,2,0,3,2,3,1,1,1,2,0,0,37,0,0],[1,3,3,2,44,38,3,0,3,3,1,32,2,33,0,1,2,36,1,2],[3,2,32,2,0,3,0,1,40,34,2,1,0,36,0,30,31,1,0,40],[2,0,3,0,0,1,3,1,3,1,3,1,3,3,2,29,28,2,3,0],[2,2,0,0,32,2,3,2,1,2,2,3,2,3,3,1,1,1,3,3],[1,3,2,32,2,1,33,0,2,1,2,30,31,3,1,1,0,2,3,1],[3,2,2,3,0,41,38,3,0,3,0,29,28,3,33,2,3,2,0,40],[2,48,0,0,0,37,3,1,1,3,2,1,1,48,36,3,3,2,33,0],[0,3,2,1,0,37,0,2,32,1,0,1,2,1,2,1,2,2,37,2],[3,2,0,3,0,36,0,1,2,3,2,0,1,2,0,1,2,40,38,0],[0,3,1,1,3,3,2,3,33,3,0,0,3,2,1,48,3,2,3,1],[0,3,0,1,3,3,1,1,36,3,3,1,3,3,0,0,0,3,0,1],[2,3,1,1,30,31,0,1,1,32,3,48,2,2,2,33,3,0,0,1],[1,2,0,1,29,28,0,2,32,3,0,40,34,3,3,36,2,1,0,48],[3,32,2,0,1,3,0,3,2,3,0,1,0,1,1,1,32,0,1,2],[1,1,2,0,1,2,3,3,2,3,2,0,1,3,1,0,3,0,1,3],[1,0,0,32,0,1,0,1,40,35,1,3,0,0,48,2,3,2,3,1]]}");
	JSONArray array = (JSONArray) jo.get("tiles");
	System.out.println(((JSONArray) array.get(1)).length());*/
	//0//h
	//1//h+1
	//2//h+2
	//3//h+3
	//h-1
    }

}
