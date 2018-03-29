/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountDynamic;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchFarmManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import de.tor.tribes.util.village.KnownVillage;

import org.apache.log4j.Logger;

import java.util.*;

/**
 *
 * @author Torridity
 */
public class TroopHelper {

	private final static Logger logger = Logger.getLogger("TroopHelper");

	public static void addNeededCatas(TroopAmountFixed units, DSWorkbenchFarmManager.FARM_CONFIGURATION pConfig,
			VillageTroopsHolder pTroops, FarmInformation pInfo) { // Not yet in use or finished

		// public static final String[] buildingNames = {"main", "barracks", "stable",
		// "workshop",
		// "church", "watchtower", "academy", "smithy", "rally", "statue", "market",
		// "timber",
		// "clay", "iron", "farm", "storage", "hide", "wall"};

		final int[] KatasNeededOther = new int[] { 0, 2, 6, 10, 15, 21, 28, 36, 45, 56, 68, 82, 98, 115, 136, 159, 185,
				215, 248, 286, 328, 376, 430, 490, 558, 634, 720, 815, 922, 1041, 1175 };
		final int[] cataMinDmg = new int[] { 0, 0, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6, 7, 8, 8, 9, 10, 10, 11,
				12, 13, 15, 16, 17, 19, 20 };
		final int[] KatasNeededHG = new int[] { 0, 0, 2, 6, 11, 17, 23, 31, 39, 49, 61, 74, 89, 106, 126, 148, 173, 202,
				234, 270, 312, 358, 410, 469, 534, 608, 691, 784, 888, 1005, 1135 };

		Village source = pTroops.getVillage();
		TroopAmountFixed backupUnits = DSWorkbenchFarmManager.getSingleton().getBackupUnits(source);
		String buildingname = DSWorkbenchFarmManager.getSingleton().getCataTarget(); // getTarget not yet
		logger.debug("The building target is " + buildingname); // implemented should give a
		// text of the target.
		int BuildingLevel = pInfo.getCataTargetBuildingLevel(buildingname);
		logger.debug("The target building is level " + BuildingLevel);
		UnitHolder catapult = DataHolder.getSingleton().getUnitByPlainName("catapult");
		int catapults = pTroops.getTroops().getAmountForUnit(catapult) - backupUnits.getAmountForUnit(catapult);
		
		if (catapults >= cataMinDmg[BuildingLevel]) { // If enough catas to down building by at least one .. Do it
			logger.debug(cataMinDmg[BuildingLevel]
					+ "catapults are needed to destroy at least one level, and the requirement is met");
			if (buildingname == "main") {
				if (BuildingLevel > 1) {
					int needed = KatasNeededHG[BuildingLevel];
					int using = Math.min(needed, catapults);
					logger.debug("The target is the main building and" + needed
							+ "catapults are needed to destroy it completely." + using + "catapults will be used");
					units.setAmountForUnit(catapult, using);
				} else { // Do not send attack if no Catas are needed
					units.fill(0);
				}

			} else {
				if (BuildingLevel > 0) {
					int needed = KatasNeededOther[BuildingLevel];
					int using = Math.min(needed, catapults);
					logger.debug("The target is an other building and" + needed
							+ "catapults are needed to destroy it completely." + using + "catapults will be used");
					units.setAmountForUnit(catapult, using);
				} else { // Do not send attack if no Catas are needed
					units.fill(0);
				}

			}

		} else { // No attack if not enough catas to bring down the building by 1
			units.fill(0);
		}

	}

	public static void addNeededRams(TroopAmountFixed units, VillageTroopsHolder pTroops, FarmInformation pInfo) {
		final int[] ramsNeeded = new int[] { 0, 2, 4, 7, 10, 14, 19, 24, 30, 37, 46, 55, 65, 77, 91, 106, 124, 143, 166,
				191, 219 };
		final int[] minDmgWall = new int[] { 0, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6 };
		Village source = pTroops.getVillage();
		TroopAmountFixed backupUnits = DSWorkbenchFarmManager.getSingleton().getBackupUnits(source);

		if (pInfo.getWallLevel() > 0) {
			UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
			int rams = pTroops.getTroops().getAmountForUnit(ram) - backupUnits.getAmountForUnit(ram);
			if (rams > 1) {
				int needed = ramsNeeded[pInfo.getWallLevel()];
				int using = Math.min(needed, rams);
				units.setAmountForUnit(ram, using);
			}
			// Check result
			if (rams < minDmgWall[pInfo.getWallLevel()]) {
				units.fill(0); // If the rams are not enough to lower the wall by at least one, no attack will
								// be send
			}
		}

	}

	public static TroopAmountFixed getTroopsForCarriage(DSWorkbenchFarmManager.FARM_CONFIGURATION pConfig,
			VillageTroopsHolder pTroops, FarmInformation pInfo) {
		TroopAmountFixed result = new TroopAmountFixed();
		TroopAmountDynamic configTroops1 = DSWorkbenchFarmManager.getSingleton().getTroops(pConfig);
		TroopAmountFixed configTroops = DSWorkbenchFarmManager.getSingleton().getMinUnits(pConfig, pTroops.getVillage());
		TroopAmountFixed backupUnits = DSWorkbenchFarmManager.getSingleton().getBackupUnits(pTroops.getVillage());

		UnitHolder[] allowed = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits(pConfig, pTroops.getVillage());
		Arrays.sort(allowed, UnitHolder.RUNTIME_COMPARATOR);

		for (UnitHolder unit : allowed) {

			if (unit.getCarry() == 0) { // Skips the loop for Spies

			} else {
				double speed = unit.getSpeed();
				// correct speed by used units (not necessary as they are sorted by runtime!?
				// ... but won't hurt anyway) -- Actually caused a bug... lol
				// speed = Math.max(speed, units.getSpeed());
				int resources = pInfo.getResourcesInStorage(System.currentTimeMillis()
						+ DSCalculator.calculateMoveTimeInMillis(pTroops.getVillage(), pInfo.getVillage(), speed));
				resources -= result.getFarmCapacity();
				int amount = 0;
				if (pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.C)) {
					amount = (int) Math.ceil((double) resources / unit.getCarry());
				} else {
					amount = configTroops.getAmountForUnit(unit);
				}

				int usable = pTroops.getTroops().getAmountForUnit(unit) - backupUnits.getAmountForUnit(unit);
				if (usable >= amount) {
					if (amount < configTroops.getAmountForUnit(unit)) {
						// If amount < min set to zero and look for other units that fulfill the requirements
						result.setAmountForUnit(unit, 0);						
					} else {
						result.setAmountForUnit(unit, amount);
						break;
					}					
				} else if (usable < amount && usable > configTroops.getAmountForUnit(unit) && DSWorkbenchFarmManager.getSingleton().allowPartlyFarming()) {
					// note: amount is for A/B and K the same as the expression on the right
					// usable cannot be > and < than amount --> A/B and K cannot get here
					// C Only gets here if Partly farming is allowed
					result.setAmountForUnit(unit, usable);
					resources -= unit.getCarry() * usable;
				} else {
					// No allowed solution found set units to zero and continue
					result.setAmountForUnit(unit, 0);
				}
				// check if farming conditions are met
				if (result.getAmountForUnit(unit) >= amount || resources <= 0) {
					logger.debug("Got carriage for all resources");
					// farm will be empty
					break;
				}
			}
		}
		if (result != null && result.hasUnits()) { // Only add spies if there are farm units
			UnitHolder spy = DataHolder.getSingleton().getUnitByPlainName("spy");
			Integer neededSpies = configTroops.getAmountForUnit(spy);
			int availableSpies = Math.max(pTroops.getTroops().getAmountForUnit(spy) - backupUnits.getAmountForUnit(spy),
					0);
			result.setAmountForUnit(spy, (neededSpies >= availableSpies) ? availableSpies : neededSpies);
		}
		return result;
	}

	public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillages(TroopAmountDynamic pMinAmounts) {
		Hashtable<Village, VillageTroopsHolder> result = new Hashtable<>();
		for (Village v : DSWorkbenchFarmManager.getSingleton().getActiveFarmGroup()) {
			VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v,
					TroopsManager.TROOP_TYPE.OWN);
			if (holder != null && hasMinTroopAmounts(holder, pMinAmounts)) {
				result.put(holder.getVillage(), holder);
			}
		}
		return result;
	}

	public static boolean hasMinTroopAmounts(VillageTroopsHolder pHolder, TroopAmountDynamic pMinAmounts) {
		if (pMinAmounts == null) {
			return true;
		}

		for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
			// check for all units amount and backup
			int amount = pHolder.getTroops().getAmountForUnit(unit);
			if ((amount - DSWorkbenchFarmManager.getSingleton().getBackupUnits(pHolder.getVillage())
					.getAmountForUnit(unit)) < pMinAmounts.getAmountForUnit(unit, pHolder.getVillage())) {
				// no troops of type or not enough units or backup met
				return false;
			}
		}
		return true;
	}

	public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillagesByCapacity(FarmInformation pInfo) {
		int currentResources = pInfo.getResourcesInStorage(System.currentTimeMillis());
		Hashtable<Village, VillageTroopsHolder> result = new Hashtable<>();

		for (Village v : DSWorkbenchFarmManager.getSingleton().getActiveFarmGroup()) {
			VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v,
					TroopsManager.TROOP_TYPE.OWN);
			if (holder != null) {
				if (holder.getTroops().getFarmCapacity(v) >= currentResources
						&& holder.getTroops().getFarmCapacity(v) <= Integer.MAX_VALUE) {
					// village is valid
					result.put(holder.getVillage(), holder);
				}
			}
		}

		return result;
	}

	public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillagesByMinHaul(int pMinHaul) {
		Village[] villages = DSWorkbenchFarmManager.getSingleton().getActiveFarmGroup();

		Hashtable<Village, VillageTroopsHolder> result = new Hashtable<>();

		for (Village v : villages) {
			VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v,
					TroopsManager.TROOP_TYPE.OWN);
			if (holder != null) {
				if (holder.getTroops().getFarmCapacity(v) >= pMinHaul
						&& holder.getTroops().getFarmCapacity(v) <= Integer.MAX_VALUE) {
					// village is valid
					result.put(holder.getVillage(), holder);
				}
			}
		}

		return result;
	}

	public static List<Village> fillSourcesWithAttacksForUnit(Village source,
			Hashtable<UnitHolder, List<Village>> villagesForUnitHolder, List<Village> existingSources,
			UnitHolder unitHolder) {
		List<Village> sourcesForUnit = existingSources != null ? existingSources
				: villagesForUnitHolder.get(unitHolder);
		if (sourcesForUnit == null) {
			sourcesForUnit = new LinkedList<>();
			sourcesForUnit.add(source);
			villagesForUnitHolder.put(unitHolder, sourcesForUnit);
		} else {
			sourcesForUnit.add(source);
		}

		return sourcesForUnit;
	}

	public static void sendTroops(Village pVillage, TroopAmountFixed pTroops) {
		VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage,
				TroopsManager.TROOP_TYPE.OWN);
		VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(pVillage,
				TroopsManager.TROOP_TYPE.IN_VILLAGE);
		VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(pVillage,
				TroopsManager.TROOP_TYPE.ON_THE_WAY);

		if (own != null) {// check for case that no troops are available at all
			own.getTroops().removeAmount(pTroops);
		}
		if (inVillage != null) {// check for case that troops are from place
			inVillage.getTroops().removeAmount(pTroops);
		}
		if (onTheWay != null) {// check for case that troops are from place
			onTheWay.getTroops().addAmount(pTroops);
		}
	}

	public static VillageTroopsHolder getRandomOffVillageTroops(Village pVillage) {
		TroopAmountFixed units = new TroopAmountFixed(0);
		for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
			if (unit.isOffense()) {
				units.setAmountForUnit(unit, (int) Math.rint(Math.random() * 7000.0 / unit.getPop()));
			}
		}
		VillageTroopsHolder holder = new VillageTroopsHolder(pVillage, new Date(System.currentTimeMillis()));
		holder.setTroops(units);
		return holder;
	}

	public static int getAttackForce(Village pVillage, UnitHolder pSlowestUnit) {
		VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage,
				TroopsManager.TROOP_TYPE.OWN);
		if (holder == null) {
			return 0;
		}

		TroopAmountFixed troops = holder.getTroops();

		int force = 0;
		for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
			int value = troops.getAmountForUnit(unit);
			if (value > 0 && unit.getSpeed() <= pSlowestUnit.getSpeed()) {
				force += unit.getAttack() * value;
			}
		}
		return force;
	}

	public static int getNeededSupports(Village pVillage, TroopAmountFixed pTargetAmount, TroopAmountFixed pSplitAmount,
			boolean pAllowSimilar) {
		boolean useArcher = !DataHolder.getSingleton().getUnitByPlainName("archer").equals(UnknownUnit.getSingleton());

		VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage,
				TroopsManager.TROOP_TYPE.IN_VILLAGE);
		TroopAmountFixed troops;
		if (holder == null) {
			troops = new TroopAmountFixed(0);
		} else {
			troops = holder.getTroops();
		}

		if (pAllowSimilar) {
			int defSplit = pSplitAmount.getDefValue();
			int defCavSplit = pSplitAmount.getDefCavalryValue();
			int defArchSplit = pSplitAmount.getDefArcherValue();

			int defDiff = pTargetAmount.getDefValue() - troops.getDefValue();
			int defCavDiff = pTargetAmount.getDefCavalryValue() - troops.getDefCavalryValue();
			int defArchDiff = pTargetAmount.getDefArcherValue() - troops.getDefArcherValue();

			int defSupport = (defDiff == 0) ? 0 : (int) (Math.ceil((double) defDiff / (double) defSplit));
			int defCavSupport = (defCavDiff == 0) ? 0 : (int) (Math.ceil((double) defCavDiff / (double) defCavSplit));
			int defArchSupport = (defArchDiff == 0) ? 0
					: (int) (Math.ceil((double) defArchDiff / (double) defArchSplit));

			int supportsNeeded = Math.max(defSupport, defCavSupport);
			if (useArcher)
				supportsNeeded = Math.max(supportsNeeded, defArchSupport);
			return supportsNeeded;
		} else {
			int supportsNeeded = 0;
			for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
				if (unit.isDefense() && !unit.getPlainName().equals("knight")) {
					int diff = pTargetAmount.getAmountForUnit(unit) - troops.getAmountForUnit(unit);
					int unitSupports = (pSplitAmount.getAmountForUnit(unit) == 0) ? 0
							: (int) (Math.ceil((double) diff / (double) pSplitAmount.getAmountForUnit(unit)));

					supportsNeeded = Math.max(supportsNeeded, unitSupports);
				}
			}

			return supportsNeeded;
		}
	}

	public static TroopAmountFixed getRequiredTroops(Village pVillage, TroopAmountFixed pTargetAmounts) {
		VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage,
				TroopsManager.TROOP_TYPE.IN_VILLAGE);
		TroopAmountFixed result = pTargetAmounts.clone();
		result.removeAmount(holder.getTroops());
		return result;
	}
}
