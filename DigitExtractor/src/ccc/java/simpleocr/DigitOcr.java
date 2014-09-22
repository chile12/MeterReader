package ccc.java.simpleocr;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bytedeco.javacpp.opencv_core.Mat;

import ccc.java.digitextractor.ImageStatics;
import ccc.java.digitextractor.data.MatchingCharacter;
import ccc.java.digitextractor.exceptions.MatrixMissmatchException;
import ccc.java.digitextractor.statichelpers.Comperators;
import ccc.java.digitextractor.statichelpers.ParallelExecuter;
import ccc.java.digitextractor.statichelpers.ParallelExecuter.Looper;

public class DigitOcr
{

	private BinaryPatternContainer patterns = new BinaryPatternContainer();
	private Map<Integer, List<List<Entry<Character, Double>>>> characterAccuracies = new HashMap<Integer, List<List<Entry<Character, Double>>>>();
	// Character - Number of same accuracy positions
	private Map<Integer, Map<Character, Integer>> bestChars = new HashMap<Integer, Map<Character, Integer>>();
	// Character - avg accuracy
	private Map<Integer, Map<Character, Double>> avgChars = new HashMap<Integer, Map<Character, Double>>();
	// Character - pos of Mat with best Val
	private Map<Integer, MatchingCharacter> preciseChars = new HashMap<Integer, MatchingCharacter>();

	public DigitOcr(Map<Character, List<byte[]>> trainingPatterns)
	{
		loadTrainingImages(patterns, trainingPatterns);
	}

	public DigitOcr(BinaryPatternContainer trainingPatterns)
	{
		this.patterns = trainingPatterns;
	}

	/**
	 * Load demo training images.
	 * 
	 * @param trainingImageDir
	 *            The directory from which to load the images.
	 */
	public void loadTrainingImages(BinaryPatternContainer target, Map<Character, List<byte[]>> patterns)
	{
		target.clear();
		for (Character chr : patterns.keySet())
		{
			List<BinaryPattern> binPat = new ArrayList<BinaryPattern>();
			List<byte[]> zw = patterns.get(chr);
			for (byte[] pat : zw)
			{
				try
				{
					binPat.add(new BinaryPattern(chr, ImageStatics.DIGIT_STORE_SIZE_WIDTH, ImageStatics.DIGIT_STORE_SIZE_HEIGHT, pat));
				}
				catch (MatrixMissmatchException ex)
				{
					ex.printStackTrace();
				}
			}
			target.put(chr, binPat);
		}
	}

	public List<Map<Character, List<Entry<Integer, Double>>>> process(int pos, final List<Mat> digits)
	{
		final List<Map<Character, List<Entry<Integer, Double>>>> res = Collections
				.synchronizedList(new ArrayList<Map<Character, List<Entry<Integer, Double>>>>());

		if (digits == null)
			return res;

		Random rand = new Random();
		while (digits.size() > 5)
		{
			digits.remove(rand.nextInt(digits.size() - 5));
		}

		ParallelExecuter.loop(0, digits.size(), new Looper()
		{
			@Override
			public void loop(int from, int to, int looperID)
			{
				{
					for (int i = from; i < to; i++)
						res.add(patterns.process(digits.get(i), i));
				}
			}
		});
		return res;
	}

	public Character GetNearestChar(int pos)
	{
		LinkedHashMap<Character, Double> chrs = (LinkedHashMap) avgChars.get(pos);
		LinkedHashMap<Character, Integer> bestchrs = (LinkedHashMap) bestChars.get(pos);
		Double bestAccuracy = ((Map.Entry<Character, Double>) chrs.entrySet().toArray()[0]).getValue();
		Character bestAvgChr = ((Map.Entry<Character, Double>) chrs.entrySet().toArray()[0]).getKey();
		List<Character> possibleChrs = new ArrayList<Character>();

		for (Character chr : chrs.keySet())
		{
			if (Math.abs(chrs.get(chr) - bestAccuracy) < bestAccuracy * 0.1)
				possibleChrs.add(chr);
		}

		for (int i = possibleChrs.size() - 1; i >= 0; i--)
		{
			if (bestchrs.get(possibleChrs.get(i)) == null)
				possibleChrs.remove(i);
		}

		if (possibleChrs.size() <= 1)
			return bestAvgChr;
		else
		{
			double bestVal = 0;
			Character posibility = bestAvgChr;
			for (int i = 0; i < possibleChrs.size(); i++)
				if (preciseChars.get(pos).getCharacter() == possibleChrs.get(i))
				{
					return preciseChars.get(pos).getCharacter();
				}
			return posibility;
		}
	}

	public Map<Character, Double> EvaluateDigitClusterAvg(List<Mat> ocrDigits, int currentPos)
	{
		avgChars.clear();
		List<Map<Character, List<Entry<Integer, Double>>>> results = process(currentPos, ocrDigits);
		Map<Character, Double> chrs = new HashMap<Character, Double>();

		for (Map<Character, List<Entry<Integer, Double>>> accuracyList : results)
		{
			for (Character chr : accuracyList.keySet())
			{
				double sum = 0;
				for (int i = 0; i < accuracyList.get(chr).size(); i++)
				{
					sum += accuracyList.get(chr).get(i).getValue();
				}
				Double current = chrs.get(chr);
				double res = sum / accuracyList.get(chr).size();
				if (current == null)
					current = Double.MAX_VALUE;
				if (res < current)
				{
					chrs.put(chr, res);
				}
			}
		}
		chrs = ImageStatics.sortMapByValue(chrs, true);
		this.avgChars.put(currentPos, chrs);
		return chrs;
	}

	public Map<Character, Integer> EvaluateDigitClusterBest(List<Mat> ocrDigits, int currentPos)
	{
		List<Map<Character, List<Entry<Integer, Double>>>> results = process(currentPos, ocrDigits);
		List<Entry<Character, Entry<Integer, Double>>> bestAccuracies = new ArrayList<Entry<Character, Entry<Integer, Double>>>();
		Map<Character, Integer> chrs = new HashMap<Character, Integer>();
		for (Map<Character, List<Entry<Integer, Double>>> accuracyList : results)
		{
			for (Character chr : accuracyList.keySet())
			{
				bestAccuracies.add(new AbstractMap.SimpleEntry<Character, Entry<Integer, Double>>(chr, accuracyList.get(chr).get(0)));
			}
		}
		Collections.sort(bestAccuracies, Comperators.EntryEntryDoubleValueComp);

		if (bestAccuracies.size() > 0)
			preciseChars.put(currentPos, new MatchingCharacter(bestAccuracies.get(0).getKey(), bestAccuracies.get(0).getValue().getKey(),
					bestAccuracies.get(0).getValue().getValue()));
		else
			preciseChars.put(currentPos, null);
		// currentPos, new AbstractMap.SimpleEntry<Character,
		// Integer>(bestAccuracies.get(0).getKey(),));

		for (int i = 0; i < 10; i++)
		{
			try
			{
				int addVal = 0;
				if (chrs.get(bestAccuracies.get(i).getKey()) != null)
					addVal = chrs.get(bestAccuracies.get(i).getKey());
				chrs.put(bestAccuracies.get(i).getKey(), ++addVal);
			}
			catch (Exception e)
			{}
		}

		chrs = ImageStatics.sortMapByValue(chrs, false);
		bestChars.put(currentPos, chrs);
		return chrs;
	}

	public Map<Integer, List<List<Entry<Character, Double>>>> getCharacterAccuracies()
	{
		return characterAccuracies;
	}

	public Map<Integer, Map<Character, Integer>> getBestChars()
	{
		return bestChars;
	}

	public Map<Integer, Map<Character, Double>> getAvgChars()
	{
		return avgChars;
	}

	public Map<Integer, MatchingCharacter> getPreciseChars()
	{
		return preciseChars;
	}

	public void clearResults()
	{
		characterAccuracies.clear();
		bestChars.clear();
		avgChars.clear();
		preciseChars.clear();
	}
}
