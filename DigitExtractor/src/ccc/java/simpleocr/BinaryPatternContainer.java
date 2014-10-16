package ccc.java.simpleocr;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bytedeco.javacpp.opencv_core.Mat;

import ccc.java.digitextractor.exceptions.MatrixMissmatchException;
import ccc.java.digitextractor.statichelpers.Comperators;

public class BinaryPatternContainer
{
	private Map<Character, List<BinaryPattern>> container = new HashMap<Character, List<BinaryPattern>>();

	public Map<Character, List<Entry<Integer, Double>>> process(Mat comp, int currentImage)
	{
		Map<Character, List<Entry<Integer, Double>>> accuracies = new HashMap<Character, List<Entry<Integer, Double>>>();
		for (Character chr : container.keySet())
		{
			List<Entry<Integer, Double>> res = new ArrayList<Entry<Integer, Double>>();
			List<BinaryPattern> pats = container.get(chr);
			for (int i = 0; i < pats.size(); i++)
			{
				try
				{
					double precision = pats.get(i).CalculateDifference(comp);
					res.add(new AbstractMap.SimpleEntry<Integer, Double>(currentImage, precision));
				}
				catch (MatrixMissmatchException e)
				{
					e.printStackTrace();
				}
			}
			Collections.sort(res, Comperators.EntryDoubleValueComp);
			accuracies.put(chr, res);
		}

		return accuracies;
	}

	public List<BinaryPattern> get(Character chr)
	{
		return container.get(chr);
	}

	public void put(Character chr, List<BinaryPattern> patterns)
	{
		container.put(chr, patterns);
	}

	public void remove(Character chr)
	{
		container.remove(chr);
	}

	public void clear()
	{
		container.clear();
	}

	public Map<Character, List<BinaryPattern>> getContainer()
	{
		return container;
	}

	public void setContainer(Map<Character, List<BinaryPattern>> container)
	{
		this.container = container;
	}
}
