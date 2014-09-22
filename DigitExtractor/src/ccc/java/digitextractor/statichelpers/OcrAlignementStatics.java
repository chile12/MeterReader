package ccc.java.digitextractor.statichelpers;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class OcrAlignementStatics
{
	public static final int SCORE_BOARD_WIDTH = 30;
	public static final int SCORE_BOARD_BONUS_10 = 10;
	public static final int SCORE_BOARD_BONUS_15 = 10;
	public static final int SCORE_BOARD_BONUS_20 = 10;
	public static final int SCORE_BOARD_BONUS_25 = 8;
	public static final int SCORE_BOARD_BONUS_30 = 6;
	public static final int SCORE_BOARD_BONUS_35 = 4;
	public static final int SCORE_BOARD_BONUS_40 = 2;
	public static final int SCORE_BOARD_BONUS_45 = 0;
	public static final int SCORE_BOARD_BONUS_50 = 0;

	public static final int ALIGNEMENT_MIN_VAL = 40;
	public static final int ALIGNEMENT_BOARDER = 50;

	public static <K, V extends Entry<?, Double>> LinkedHashMap<K, V> sortMapOfEntiesByValueValue(Map<K, V> map, boolean ascending)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		if (ascending)
		{
			Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o1.getValue().getValue()).compareTo(o2.getValue().getValue());
				}
			});
		}
		else
		{
			Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o2.getValue().getValue()).compareTo(o1.getValue().getValue());
				}
			});
		}

		LinkedHashMap<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
