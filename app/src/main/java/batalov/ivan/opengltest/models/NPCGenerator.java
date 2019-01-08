package batalov.ivan.opengltest.models;

import android.content.Context;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import batalov.ivan.opengltest.MyUtilities;
import batalov.ivan.opengltest.R;

/**
 * Created by ivan on 4/6/18.
 */

public class NPCGenerator {

	public static ArrayList<NPC> generateNPCs(Context context, int count, ArrayList<Integer> availableBlockNums, ArrayList<MapBlock> blocks){
		ArrayList<NPC> npcList = new ArrayList();
		Model3D model = new Model3D(context, R.raw.sphere_1x1, false);

		if(availableBlockNums == null){
			availableBlockNums = new ArrayList<>(blocks.size());
			for(int i = 0; i < blocks.size(); i++){
				availableBlockNums.add(i);
			}
		}

		Random r = new Random();
		if(count >= availableBlockNums.size()) {
			int minNPCPerBlock = count/availableBlockNums.size();

			// create equal amount of NPCs per block to make the remaining number of NPCs less than the number of available blocks
			for(int i = 0; i < availableBlockNums.size(); i++){
				MapBlock block = blocks.get(availableBlockNums.get(i));
				for(int j = 0; j < minNPCPerBlock; j++){
					float[] position = block.generateNPCLocation(r.nextFloat());
					//System.out.println("NPC position: (" + position[0] + ", " + position[1] + ", " + position[2] + ");");
					NPC npc = new NPC(model, position);
					float[][] path = MyUtilities.convertCoordArray(block.getCenterPath(), 3);

					// find closest point along the center path that's accessible along the straight line
					// in future need to check if the point can be reached as the crow flies
					// but for that I'd need to pass leveMap here...
					int nextPoint = 0;
					float minDist = MyUtilities.distance(path[0], position, 2);
					for(int pointNum = 1; pointNum < path.length; pointNum++){
						float tempDist = MyUtilities.distance(path[pointNum], position, 2);
						if(tempDist < minDist){
							nextPoint = pointNum;
							minDist = tempDist;
						}
					}
					npc.addMovementPattern(path, nextPoint, 1);
					npcList.add(npc);
				}
			}
			count -= minNPCPerBlock*availableBlockNums.size();
		}

		for (int i = 0; i < count; i++) {
			int nextIndex = r.nextInt(availableBlockNums.size());
			int blockNum = availableBlockNums.get(nextIndex);
			availableBlockNums.remove(nextIndex);
			MapBlock block = blocks.get(blockNum);
			float[] position = block.generateNPCLocation(r.nextFloat());
			NPC npc = new NPC(model, position);
			npc.addMovementPattern(MyUtilities.convertCoordArray(block.getCenterPath(), 3), 0, 1);
			npcList.add(npc);
		}
		return npcList;
	}
}
