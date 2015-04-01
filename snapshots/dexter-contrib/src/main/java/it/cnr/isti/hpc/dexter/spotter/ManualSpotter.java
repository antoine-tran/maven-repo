package it.cnr.isti.hpc.dexter.spotter;

import java.io.File;

import tuan.io.FileUtility;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.common.NamedDocument;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.entity.EntityRanker;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepository;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepositoryFactory;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.structure.LRUCache;

/**
 * A "manual spotter" that simply reads the ground truth of human annotations to
 * build the spots. The tagger that uses this spotter will be just a disambiguator
 * with a "perfect" spotter 
 * 
 * @author tuan
 *
 */
public class ManualSpotter extends AbstractSpotter {

	private static LRUCache<String, Spot> cache;

	DexterParams params;

	SpotRepository spotRepo;
	
	public ManualSpotter() {
		params = DexterParams.getInstance();
		int cachesize = params.getCacheSize("spotter");		
		cache = new LRUCache<String, Spot>(cachesize);
		SpotRepositoryFactory factory = new SpotRepositoryFactory();
		spotRepo = factory.getStdInstance();
	}
	
	@Override
	public SpotMatchList match(DexterLocalParams p,
			Document doc) {
		
		// Manual spotter only supports a certain types of document, i.e. NamedDocument
		if (!(doc instanceof NamedDocument)) {
			throw new IllegalArgumentException("manual spotter only supports "
					+ "some types of document: NamedDocument");
		}
		NamedDocument document = (NamedDocument)doc;
		SpotMatchList matches = new SpotMatchList();
		
		// Named document is a type of flat document, and so has only one field which is
		// "body"
		Field field = document.getField("body");
		EntityRanker er = new EntityRanker(field);
		
		if (!params.hasExtraParam("manual.spotter.groundtruth")) {
			throw new IllegalArgumentException("The required parameter" +
					" manual.spotter.groundtruth not found");
		}
		String groundTruthDir = params.getExtraParamValue("manual.spotter.groundtruth");
		
		Spot s;
		
		// the ground truth is formatted as CSV file, each line has:
		// [annotation] TAB [starting offset] TAB [end offset]
		for (String line : FileUtility.readLines(groundTruthDir + File.separator
				+ document.getDocname())) {
			int i = line.indexOf('\t');
			int j = line.lastIndexOf('\t');
			String text = SpotManager.cleanText(line.substring(0,i));
			if (cache.containsKey(text)) {
				// hit in cache
				s = cache.get(text);
				if (s != null) {
					s = s.clone();
				}
			} else {
				s = spotRepo.getSpot(text);
				cache.put(text, s);
			}

			if (s == null) {
				continue;
			}
			
			SpotMatch match = new SpotMatch(s, field);
			EntityMatchList entities = er.rank(match);
			match.setEntities(entities);
			match.setStart(Integer.parseInt(line.substring(i+1,j)));
			match.setEnd(Integer.parseInt(line.substring(j+1)));
			matches.add(match);
		}	
		return matches;
	}

	@Override
	public void init(DexterParams paramDexterParams,
			DexterLocalParams paramDexterLocalParams) {
		// TODO Auto-generated method stub	
	}
}
