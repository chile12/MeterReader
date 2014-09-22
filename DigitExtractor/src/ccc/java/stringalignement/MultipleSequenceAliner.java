package ccc.java.stringalignement;

import java.security.InvalidParameterException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import ccc.java.digitextractor.ImageStatics;
import ccc.java.digitextractor.data.MatchingCharacter;
import ccc.java.digitextractor.exceptions.NotProcessedException;
import ccc.java.digitextractor.statichelpers.OcrAlignementStatics;

public class MultipleSequenceAliner
{
	private List<MatchingCharacter> anchorRowA;
	private int anchorRowAStartPos;
	private List<MatchingCharacter> anchorRowB;
	private int anchorRowBStartPos;
	private List<int[][]> scoreBoard = new ArrayList<int[][]>();
	private List<LinkedHashMap<Integer, Entry<Integer, Double>>> evaluatedScores = new ArrayList<LinkedHashMap<Integer, Entry<Integer, Double>>>();
	private List<List<MatchingCharacter>> firstPrecs = new ArrayList<List<MatchingCharacter>>();
	private LinkedHashMap<Integer, Double> positions = new LinkedHashMap<Integer, Double>();
	private List<List<MatchingCharacter>> precisions = new ArrayList<List<MatchingCharacter>>();
	private List<List<Double>> alignments = new ArrayList<List<Double>>();
	private List<NeedlemanWunsch> nws = new ArrayList<NeedlemanWunsch>();
	private float thresholdVal;

	public MultipleSequenceAliner(int expectedSequenceSize)
	{
		this.thresholdVal = ((float) expectedSequenceSize) * 1f / 2f;
	}

	public void alignResults()
	{
		int pos = alignments.size();

		subtractPrecisionBoni();

		if (anchorRowA != null)
			buildScoreBoard(pos);
		else
			return;

		evaluateScoreBoard();
	}

	public void addMatchingCharacterList(List<MatchingCharacter> list)
	{
		this.precisions.add(list);
	}

	private void evaluateScoreBoard()
	{
		evaluatedScores.clear();
		for (int j = 0; j < OcrAlignementStatics.SCORE_BOARD_WIDTH; j++)
		{
			LinkedHashMap<Integer, Entry<Integer, Double>> posMap = new LinkedHashMap<Integer, Entry<Integer, Double>>();
			posMap.put((int) '0', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '1', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '2', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '3', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '4', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '5', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '6', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '7', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '8', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));
			posMap.put((int) '9', new SimpleEntry<Integer, Double>(0, Double.MAX_VALUE));

			for (int i = 0; i < scoreBoard.size(); i++)
			{
				int chr = scoreBoard.get(i)[j][0];
				if (chr != (int) '\u0000' && chr != (int) '-')
				{
					posMap.put(
							scoreBoard.get(i)[j][0],
							new SimpleEntry<Integer, Double>(posMap.get(scoreBoard.get(i)[j][0]).getKey() + 1, Math.min(scoreBoard.get(i)[j][1],
									posMap.get(scoreBoard.get(i)[j][0]).getValue())));
				}
			}

			posMap = OcrAlignementStatics.sortMapOfEntiesByValueValue(posMap, true);

			// int chr = 0;
			// double val = 0d;
			// for (int ch : posMap.keySet()) {
			// if (posMap.get(ch).getKey() > 0) {
			// if (val < 100 - ((double) posMap.get(ch).getValue() / (double)
			// posMap
			// .get(ch).getKey())) {
			// val = 100 - ((double) posMap.get(ch).getValue() / (double) posMap
			// .get(ch).getKey());
			// posMap.put(ch, new SimpleEntry<Integer, Double>(posMap
			// .get(ch).getKey(), val));
			// chr = ch;
			// }
			// }
			// }
			if (((Entry<Integer, Double>) (posMap.values().toArray()[0])).getKey() > 0)
				positions.put(j, ((Entry<Integer, Double>) (posMap.values().toArray()[0])).getValue());
			evaluatedScores.add(posMap);
		}

		positions = ImageStatics.sortMapByValue(positions, false);
	}

	public String getCurrentResult()
	{
		if (this.anchorRowA == null)
			return MatchingCharacter.extractString(this.precisions.get(this.precisions.size() - 1));
		String ret = "";
		if (positions.size() == 0)
			return ret;

		float thresholdValue = OcrAlignementStatics.ALIGNEMENT_MIN_VAL;// ((float)
		// ((Integer)
		// positions.values().toArray()[0]).intValue()
		// /
		// 4f);

		int minPos = Integer.MAX_VALUE;
		int maxPos = 0;

		for (int i = 0; i < positions.size(); i++)
		{
			@SuppressWarnings("unchecked")
			Entry<Integer, Double> entry = (Entry<Integer, Double>) (positions.entrySet().toArray()[i]);

			double zw = (double) entry.getValue();
			if (entry.getKey() > 5 && zw < thresholdValue)
			{
				if (minPos > entry.getKey())
					minPos = entry.getKey();
				if (maxPos < entry.getKey())
					maxPos = entry.getKey();
			}
		}
		for (int i = minPos; i <= maxPos; i++)
		{
			Object[] lala = evaluatedScores.get(i).keySet().toArray();
			ret += (char) ((int) (evaluatedScores.get(i).keySet().toArray()[0]));
		}

		return ret;
	}

	private void buildScoreBoard(int pos)
	{
		for (int i = pos; i < precisions.size(); i++) // only align unaligned
														// results
		{
			NeedlemanWunsch anw = new NeedlemanWunsch(anchorRowA, precisions.get(i));
			NeedlemanWunsch bnw = new NeedlemanWunsch(anchorRowB, precisions.get(i));
			anw.process();
			anw.backtrack();
			if (anw.getMatchCount() > 2)
				try
				{
					scoreBoard.add(anw.getAlignedCompRow(OcrAlignementStatics.SCORE_BOARD_WIDTH, anchorRowAStartPos));
				}
				catch (InvalidParameterException | NotProcessedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			bnw.process();
			bnw.backtrack();
			if (bnw.getMatchCount() > 2)
				try
				{
					scoreBoard.add(bnw.getAlignedCompRow(OcrAlignementStatics.SCORE_BOARD_WIDTH, anchorRowBStartPos));
				}
				catch (InvalidParameterException | NotProcessedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private void subtractPrecisionBoni()
	{
		int pos = alignments.size();
		for (int i = pos; i < precisions.size(); i++) // only align unaligned
														// results
		{
			List<MatchingCharacter> match = precisions.get(i);
			List<Double> all = new ArrayList<Double>();
			for (int j = 0; j < match.size(); j++)
			{
				if (match.get(j) != null)
				{
					double newPrecision = match.get(j).getPrecision();
					if (j > 0 && match.get(j - 1) != null)
						newPrecision -= getBonusPoints(match.get(j - 1).getPrecision());
					if (j < match.size() - 1 && match.get(j + 1) != null)
						newPrecision -= getBonusPoints(match.get(j + 1).getPrecision());
					all.add(newPrecision);
				}
				else
					all.add(100d);
			}
			for (int j = 0; j < match.size(); j++)
			{
				if (match.get(j) != null)
				{
					if (all.get(j) < OcrAlignementStatics.ALIGNEMENT_MIN_VAL)
					{
						match.get(j).setPrecision(all.get(j));
						continue;
					}
					if (all.get(j) < OcrAlignementStatics.ALIGNEMENT_BOARDER
							&& (j > 0 && all.get(j - 1) < OcrAlignementStatics.ALIGNEMENT_MIN_VAL || j < all.size() - 1
									&& all.get(j + 1) < OcrAlignementStatics.ALIGNEMENT_MIN_VAL))
					{
						match.get(j).setPrecision(all.get(j));
						continue;
					}
					match.remove(j);
					match.add(j, null);
				}
			}
			List<MatchingCharacter> zw = new ArrayList<MatchingCharacter>();
			zw.addAll(match);
			zw.removeAll(Collections.singleton(null));
			if (zw.size() < 3)
			{
				precisions.remove(i);
				continue;
			}
			if (firstPrecs.size() < 5 && zw.size() > thresholdVal)
				firstPrecs.add(match);

			if (firstPrecs.size() == 5)
				evaluateFirstEntries();

			alignments.add(all);
		}
	}

	private void evaluateFirstEntries()
	{
		double bestRowVal = 0d;
		NeedlemanWunsch bestNw = null;
		for (int i = 0; i < firstPrecs.size(); i++) // only align unaligned
													// results
		{
			for (int j = i; j < firstPrecs.size(); j++)
			{
				if (i != j)
				{
					NeedlemanWunsch nw = new NeedlemanWunsch(firstPrecs.get(i), firstPrecs.get(j));
					nw.process();
					nw.backtrack();
					nws.add(nw);
					double rowMatchVal = nw.getRowMatchValue();
					if (bestRowVal < rowMatchVal)
					{
						bestRowVal = rowMatchVal;
						bestNw = nw;
					}
				}
			}
		}

		if (bestNw.getAnchorShiftedRight() <= bestNw.getCompSeqShiftedRight())
		{
			anchorRowA = bestNw.getaRow();
			anchorRowB = bestNw.getbRow();
		}
		else
		{
			anchorRowA = bestNw.getbRow();
			anchorRowB = bestNw.getaRow();
		}
		anchorRowAStartPos = (int) ((float) OcrAlignementStatics.SCORE_BOARD_WIDTH / 2f - (float) (bestNw.getmAlignmentSeqA().length()
				- bestNw.getAnchorShiftedLeft() - bestNw.getAnchorShiftedRight()) / 2f);
		anchorRowBStartPos = anchorRowAStartPos + bestNw.getCompSeqShiftedRight();

		for (NeedlemanWunsch nw : nws)
		{
			try
			{
				if (nw.getaRow() == anchorRowA)
				{
					if (nw.getMatchCount() > 2)
						scoreBoard.add(nw.getAlignedCompRow(OcrAlignementStatics.SCORE_BOARD_WIDTH, anchorRowAStartPos));
				}
				if (nw.getbRow() == anchorRowB)
				{
					NeedlemanWunsch n = new NeedlemanWunsch(anchorRowB, nw.getaRow());
					n.process();
					n.backtrack();
					if (n.getMatchCount() > 2)
						scoreBoard.add(n.getAlignedCompRow(OcrAlignementStatics.SCORE_BOARD_WIDTH, anchorRowBStartPos));
				}
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		nws = null;
		firstPrecs.add(null);
	}

	private double getBonusPoints(double precision)
	{
		if (precision < 10)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_10;
		if (precision < 15)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_15;
		if (precision < 20)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_20;
		if (precision < 25)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_25;
		if (precision < 30)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_30;
		if (precision < 35)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_35;
		if (precision < 40)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_40;
		if (precision < 45)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_45;
		if (precision < 50)
			return OcrAlignementStatics.SCORE_BOARD_BONUS_50;
		return 0;
	}

	// DEBUG
	public void printScoreboards()
	{
		System.out.println("ScoreBoard = \n\n");
		for (int i = 0; i < scoreBoard.size(); i++)
		{
			String s = "";
			for (int j = 0; j < OcrAlignementStatics.SCORE_BOARD_WIDTH; j++)
			{
				s += new Character((char) scoreBoard.get(i)[j][0]);
				s += " ";
			}
			System.out.println(s);
		}
	}
}
