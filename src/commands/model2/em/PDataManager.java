package commands.model2.em;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import commands.model2.gm.IRLModule;
import commands.model2.gm.IRLModule.BehaviorValue;
import commands.model2.gm.MMNLPModule;
import commands.model2.gm.MMNLPModule.StringValue;
import commands.model2.gm.CommandsModelConstructor;
import commands.model2.gm.StateRVValue;
import commands.model2.gm.TAModule;
import generativemodel.GMQuery;
import generativemodel.GMQueryResult;
import generativemodel.GenerativeModel;
import generativemodel.RVariable;
import generativemodel.RVariableValue;

public class PDataManager {

	protected GenerativeModel			gm;
	protected Map <Integer, Double>		trainingDataProbs;
	
	
	public PDataManager(GenerativeModel gm) {
		this.gm = gm;
		this.resetTrainingDataProbs();
	}
	
	
	public void resetTrainingDataProbs(){
		System.out.println("Resetting Training Data probs");
		this.trainingDataProbs = new HashMap<Integer, Double>();
	}
	
	
	public double getProbForData(int dataInstanceId, List<RVariableValue> observables){
		
		Double cached = trainingDataProbs.get(dataInstanceId);
		if(cached != null){
			return cached;
		}
		
		//if not already cached then we need to compute it
		StringValue command = (StringValue)this.getCommandValue(observables);
		BehaviorValue behavior = (BehaviorValue)this.getBehaviorValue(observables);
		StateRVValue srvv = new StateRVValue(behavior.t.getState(0), gm.getRVarWithName(CommandsModelConstructor.STATERVNAME));
		
		
		//System.out.println(command.toString());
		
		double sumP = 0.;
		
		List <RVariableValue> sconds = new ArrayList<RVariableValue>();
		sconds.add(srvv);
		
		
		
		Iterator<GMQueryResult> htIterRes = gm.getNonZeroIterator(gm.getRVarWithName(TAModule.HTNAME), sconds, true);
		while(htIterRes.hasNext()){
			GMQueryResult hres = htIterRes.next();
			RVariableValue htv = hres.getSingleQueryVar();
			List <RVariableValue> hsConds = new ArrayList<RVariableValue>();
			hsConds.add(srvv);
			hsConds.add(htv);
			
			
			Iterator<GMQueryResult> abConIterRes = gm.getNonZeroIterator(gm.getRVarWithName(TAModule.ACNAME), hsConds, true);
			while(abConIterRes.hasNext()){
				GMQueryResult abcRes = abConIterRes.next();
				
				

				Iterator<GMQueryResult> abGIterRes = gm.getNonZeroIterator(gm.getRVarWithName(TAModule.AGNAME), hsConds, true);
				while(abGIterRes.hasNext()){
					GMQueryResult abgRes = abGIterRes.next();

					
					List <RVariableValue> abConds = new ArrayList<RVariableValue>();
					abConds.add(abcRes.getSingleQueryVar());
					abConds.add(abgRes.getSingleQueryVar());
					abConds.add(srvv);
					
					
					Iterator<GMQueryResult> cIterRes = gm.getNonZeroIterator(gm.getRVarWithName(TAModule.CNAME), abConds, true);
					while(cIterRes.hasNext()){
						GMQueryResult cRes = cIterRes.next();
						
						
						Iterator<GMQueryResult> gIterRes = gm.getNonZeroIterator(gm.getRVarWithName(TAModule.GNAME), abConds, true);
						while(gIterRes.hasNext()){
							GMQueryResult gRes = gIterRes.next();
							
							double jp = hres.probability*abcRes.probability*abgRes.probability*cRes.probability*gRes.probability;
							
							List <RVariableValue> taskConds = new ArrayList<RVariableValue>();
							taskConds.add(cRes.getSingleQueryVar());
							taskConds.add(gRes.getSingleQueryVar());
							taskConds.add(srvv);
							
							GMQuery commandQuery = new GMQuery();
							commandQuery.addQuery(command);
							commandQuery.setConditions(taskConds);
							
							GMQueryResult commandRes = this.gm.getProb(commandQuery, true);
							
							
							double sumRB = 0.;
							Iterator<GMQueryResult> rIterRes = gm.getNonZeroIterator(gm.getRVarWithName(TAModule.RNAME), taskConds, true);
							while(rIterRes.hasNext()){
								GMQueryResult rRes = rIterRes.next();
								
								
								
								GMQuery bQuery = new GMQuery();
								bQuery.addQuery(behavior);
								bQuery.addCondition(rRes.getSingleQueryVar());
								bQuery.addCondition(srvv);
								
								GMQueryResult bRes = this.gm.getProb(bQuery, true);
								
								//System.out.println(rRes.getSingleQueryVar().toString() + ": " + bRes.probability);
								
								sumRB += rRes.probability*bRes.probability;
								
							}
							
							sumP += jp*commandRes.probability*sumRB;
							
							
							
						}
					}
					
				}
				
				
			}
						
			
		}
		
		
		trainingDataProbs.put(dataInstanceId, sumP);
		
		
		
		return sumP;
		
	}
	
	
	public RVariableValue getCommandValue(List<RVariableValue> observables){
		RVariable command = gm.getRVarWithName(MMNLPModule.CNAME);
		
		for(RVariableValue rv : observables){
			if(rv.isValueFor(command)){
				return rv;
			}
		}
		
		return null;
		
	}
	
	public RVariableValue getBehaviorValue(List<RVariableValue> observables){
		RVariable behavior = gm.getRVarWithName(IRLModule.BNAME);
		
		for(RVariableValue rv : observables){
			if(rv.isValueFor(behavior)){
				return rv;
			}
		}
		
		return null;
		
	}
	
	
	

}
