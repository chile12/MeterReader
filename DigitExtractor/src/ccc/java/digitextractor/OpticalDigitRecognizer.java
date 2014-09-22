package ccc.java.digitextractor;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;

import ccc.java.digitextractor.data.BackGroundShade;
import ccc.java.digitextractor.data.RectExt;
import ccc.java.digitextractor.exceptions.FrameNotFoundException;
import ccc.java.digitextractor.statichelpers.Comperators;
import ccc.java.digitextractor.statichelpers.ParallelExecuter;
import ccc.java.digitextractor.statichelpers.ParallelExecuter.Looper;
import ccc.java.kmeans.ClusterDimension;
import ccc.java.kmeans.ClusteringException;
import ccc.java.kmeans.KPoint;
import ccc.java.kmeans.KPointCollection;
import ccc.java.simpleocr.DigitOcr;

public class OpticalDigitRecognizer
{
	private int numberOfDigitsToRecognize;
	private Mat image;
	private BackGroundShade backGround;
	private Map<Rect, List<Rect>> clusters;
	private List<Rect> clusterKeys;
	private DigitOcr ocr;

	public OpticalDigitRecognizer(Mat image, int numberOfDigitsToRecognize, BackGroundShade shade, DigitOcr ocr)
	{
		this.numberOfDigitsToRecognize = numberOfDigitsToRecognize;
		this.image = image;
		this.backGround = shade;
		this.ocr = ocr;
	}

	public List<List<Mat>> ExtractDigits() throws FrameNotFoundException
	{
		final List<Rect> boundingBoxes = Collections.synchronizedList(new ArrayList<Rect>());
		int loopcount = 256 / ImageStatics.BRIGHTNESS_STEP_DISTANCE;

		ParallelExecuter.loop(1, loopcount, new Looper()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void loop(int from, int to, int looperId)
			{
				for (int i = from; i < to; i++)
				{
					int brightness = i * ImageStatics.BRIGHTNESS_STEP_DISTANCE - 128;
					Mat workingImage = ImageStatics.ChangeBrightnessContrast(image, brightness, 128);
					// Laplacian(workingImage, workingImage,
					// workingImage.depth(), 1, 1, 0, BORDER_DEFAULT);
					List<CvRect> newBBs = ImageStatics.getBoundingBoxesOfContoures(workingImage, backGround);
					List<? extends Rect> newBBa = RectExt.ConvertCvRectList(newBBs, brightness);
					deleteWrongSizedBoxes(image.size(), (List<Rect>) newBBa);
					boundingBoxes.addAll((Collection<? extends RectExt>) newBBa);
				}
			}

		});
		if (ImageStatics.DEV_MODE)
		{
			ImageStatics.putOutDigitsAsFiles(image, boundingBoxes, ImageStatics.ROOT + "\\extracted");
		}
		if (boundingBoxes.size() == 0)
			return null;
		Date d = new Date();
		List<Rect> orderedBoxes = getOrderedBoxFrameList(boundingBoxes);

		clusterDigits(orderedBoxes);
		List<List<Rect>> rectClusters = evaluateClusters();
		List<List<Mat>> orderedDigits = ImageStatics.getImagesFromFrameList(rectClusters, image);
		scaleBinaryImageList(orderedDigits);
		if (ImageStatics.DEV_MODE)
		{
			System.out.println(new Date().getTime() - d.getTime());
			for (int i = 0; i < orderedDigits.size(); i++)
				ImageStatics.putOutDigitsAsFiles(orderedDigits.get(i), ImageStatics.ROOT + "\\ordered\\" + String.valueOf(i) + "\\", null);
		}
		return orderedDigits;
	}

	private List<Rect> deleteWrongSizedBoxes(Size frame, List<Rect> boxes)
	{
		for (int i = boxes.size() - 1; i >= 0; i--)
		{
			Rect boxi = boxes.get(i);
			float heightCoef = ((float) frame.height()) / ((float) boxi.height());
			float ratio = ((float) boxi.height()) / ((float) boxi.width());
			if (boxi.height() < ImageStatics.DIGIT_MIN_HEIGHT_PIXELS || heightCoef <= ImageStatics.DIGIT_MIN_HEIGHT
					|| heightCoef > ImageStatics.DIGIT_MAX_HEIGHT || ratio <= ImageStatics.DIGIT_MIN_RATIO || ratio > ImageStatics.DIGIT_MAX_RATIO)
				boxes.remove(i);
		}
		return boxes;
	}

	private List<Rect> getOrderedBoxFrameList(List<Rect> boundingBoxes)
	{
		List<Rect> frames = new ArrayList<Rect>();
		for (int i = 0; i < boundingBoxes.size(); i++)
		{
			Rect boxFrame = boundingBoxes.get(i);
			if (i == 0)
				frames.add(boxFrame);
			else
			{
				if (boxFrame.x() >= frames.get(i - 1).x())
					frames.add(boxFrame);
				else
				{
					for (int j = 0; j < i; j++)
						if (frames.get(j).x() > boxFrame.x())
						{
							frames.add(j, boxFrame);
							break;
						}
				}
			}
		}
		return frames;
	}

	private List<KPointCollection> clusterRecordsByVariation(KPointCollection records, double relativeWidth, double relativeHeight,
			Double xVariationPrecentage, Double yVariationPrecentage)
	{
		return clusterRecordsByVariation(records, relativeWidth, relativeHeight, xVariationPrecentage, yVariationPrecentage, 1);
	}

	private List<KPointCollection> clusterRecordsByVariation(KPointCollection records, double relativeWidth, double relativeHeight,
			Double xVariationPrecentage, Double yVariationPrecentage, int clusterSizeMinVal)
	{
		ccc.java.kmeans.KMeans hmeans = null;
		try
		{
			hmeans = new ccc.java.kmeans.KMeans(records, relativeWidth, relativeHeight, xVariationPrecentage, yVariationPrecentage);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClusterDimension dim = ClusterDimension.Area;
		if (xVariationPrecentage == null)
			dim = ClusterDimension.Yaxis;
		if (yVariationPrecentage == null)
			dim = ClusterDimension.Xaxis;
		List<KPointCollection> heightClusters = new ArrayList<KPointCollection>();
		try
		{
			heightClusters = hmeans.GetClusters(dim);
		}
		catch (ClusteringException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = heightClusters.size() - 1; i >= 0; i--)
			if (heightClusters.get(i).size() < clusterSizeMinVal)
				heightClusters.remove(i);

		return heightClusters;
	}

	private void clusterDigits(List<Rect> frames)
	{
		KPointCollection records = new KPointCollection();

		for (int i = 0; i < frames.size(); i++)
			records.add(new KPoint(i, (double) frames.get(i).x(), (double) frames.get(i).y()));
		List<KPointCollection> xPosClusters = clusterRecordsByVariation(records, image.size().width(), image.size().height(),
				ImageStatics.CLUSTER_X_VARIATION_PERC, null);

		Map<Rect, List<Rect>> holes = new HashMap<Rect, List<Rect>>();

		for (KPointCollection coll : xPosClusters)
		{
			Rect key = frames.get(coll.getCentroid().getId());
			holes.put(key, new ArrayList<Rect>());
			for (KPoint point : coll)
			{
				holes.get(key).add(frames.get(point.getId()));
			}
		}
		clusters = holes;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<List<Rect>> evaluateClusters()
	{
		List<List<Rect>> output = new ArrayList<List<Rect>>();
		clusterKeys = new ArrayList(Arrays.asList(((Rect[]) clusters.keySet().toArray(new Rect[0]))));

		Collections.sort(clusterKeys, Comperators.Xcomp);
		KPointCollection records = new KPointCollection();

		for (int i = 0; i < clusterKeys.size(); i++)
		{
			List<Rect> cluster = clusters.get(clusterKeys.get(i));
			for (int j = 0; j < cluster.size(); j++)
			{
				KPoint p = new KPoint(((RectExt) (cluster.get(j))).getId(), (double) cluster.get(j).y() + cluster.get(j).height(),
						(double) (cluster.get(j).y()), cluster.get(j));
				p.setCluster(i);
				records.add(p);
			}
			if (ImageStatics.DEV_MODE)
			{
				ImageStatics.putOutDigitsAsFiles(image, cluster, ImageStatics.ROOT + "\\rawClusters\\" + String.valueOf(clusterKeys.get(i).x()));
			}
		}

		List<KPointCollection> ys = clusterRecordsByVariation(records, image.size().width(), image.size().height(), null,
				ImageStatics.CLUSTER_INTERNAL_HEIGHT_VARIATION_PERC, records.size() / 10);

		Collections.sort(ys, Comperators.KpointCollectionYComp);
		int yStart = (int) ys.get(0).getCentroid().getY();

		ys = clusterRecordsByVariation(records, image.size().width(), image.size().height(), ImageStatics.CLUSTER_INTERNAL_HEIGHT_VARIATION_PERC,
				null);

		Collections.sort(ys, Comperators.KpointCollectionXComp);
		int yEnd = (int) ys.get(0).getCentroid().getX();

		records.clear();
		for (int i = 0; i < clusterKeys.size(); i++)
		{
			List<Rect> cluster = clusters.get(clusterKeys.get(i));
			for (int j = 0; j < cluster.size(); j++)
			{
				Rect r = cluster.get(j);
				if (r.y() >= yStart)
				{
					KPoint p = new KPoint(((RectExt) (r)).getId(), (double) r.width(), (double) (r.height()), r);
					p.setCluster(i);
					records.add(p);
				}
			}
			if (ImageStatics.DEV_MODE)
			{
				ImageStatics.putOutDigitsAsFiles(image, cluster, ImageStatics.ROOT + "\\rawClusters\\" + String.valueOf(clusterKeys.get(i).x()));
			}
		}
		// reduce possible bounding boxes further by clustering by height
		reduceByHeightClustering(records, yEnd - yStart);

		int clusterSizeMinVal = 0;
		List<Float> medianArray = new ArrayList<Float>();
		for (int i = 0; i < clusterKeys.size(); i++)
		{
			if (clusters.get(clusterKeys.get(i)).size() > 0)
			{
				medianArray.add((float) clusters.get(clusterKeys.get(i)).size());
			}
			if (ImageStatics.DEV_MODE)
			{
				ImageStatics.putOutDigitsAsFiles(image, clusters.get(clusterKeys.get(i)),
						ImageStatics.ROOT + "\\reducedHeights\\" + String.valueOf(clusterKeys.get(i).x()));
			}
		}
		Collections.sort(medianArray);

		// TODO rework this!!
		if (numberOfDigitsToRecognize == 0)
			numberOfDigitsToRecognize = 10;
		int digitCount = Math.min(this.numberOfDigitsToRecognize, medianArray.size());
		if (digitCount == 0)
			return output; // size == 0
		for (int i = medianArray.size() - digitCount; i < medianArray.size(); i++)
			clusterSizeMinVal += medianArray.get(i);
		clusterSizeMinVal = (int) (clusterSizeMinVal / digitCount * 0.5);
		// till here!

		evaluateClusterPositions(clusterSizeMinVal);
		Collections.sort(clusterKeys, Comperators.Xcomp);

		for (int i = 0; i < clusterKeys.size(); i++)
		{
			if (clusterKeys.get(i).height() == 0)
				output.add(null);
			else if (clusters.get(clusterKeys.get(i)).size() > 0)
			{
				output.add(clusters.get(clusterKeys.get(i)));
				List<RectExt> cluster = ImageStatics.extractExtendedCvRect(clusters.get(clusterKeys.get(i)));
				// /DEBUG
				if (ImageStatics.DEV_MODE)
				{
					List<Mat> images = ImageStatics.getImagesFromFrames(cluster, image.clone());
					ImageStatics.putOutDigitsAsFiles(images, ImageStatics.ROOT + "\\clusters\\" + String.valueOf(clusterKeys.get(i).x()), null);
				}
				// /END DEBUG
			}
			else
				output.add(null);
		}
		return output;
	}

	private void reduceByHeightClustering(KPointCollection records, int height)
	{
		ccc.java.kmeans.KMeans hmeans = null;

		try
		{
			hmeans = new ccc.java.kmeans.KMeans(records, 0d, (double) height, null, ImageStatics.CLUSTER_INTERNAL_HEIGHT_VARIATION_PERC);

			final List<KPointCollection> heightClusters = hmeans.GetClusters(ClusterDimension.Yaxis);
			final List<Entry<Integer, Double>> similarityVals = Collections.synchronizedList(new ArrayList<Entry<Integer, Double>>());

			ParallelExecuter.loop(0, heightClusters.size(), new Looper()
			{
				@Override
				public void loop(int from, int to, int looperID)
				{
					for (int i = from; i < to; i++)
					{
						KPointCollection heights = heightClusters.get(i);
						if (heights.size() >= numberOfDigitsToRecognize)
						{
							List<RectExt> ocrTest = new ArrayList<RectExt>();
							ocrTest.add((RectExt) (heights.get(0).getReference()));
							ocrTest.add((RectExt) (heights.get(heights.size() - 1).getReference()));
							ocrTest.add((RectExt) (heights.get(heights.size() / 2).getReference()));
							List<Mat> imgs = ImageStatics.getImagesFromFrames(ocrTest, image);
							scaleBinaryImages(imgs);
							Map<Character, Double> evalVals = ocr.EvaluateDigitClusterAvg(imgs, 0);
							Double[] vals = new Double[evalVals.values().size()];
							evalVals.values().toArray(vals);
							double val = 0;
							for (Double dd : vals)
								val += dd;
							Entry zw = new AbstractMap.SimpleEntry<Integer, Double>(i, val / vals.length);
							if (zw != null)
								similarityVals.add(zw);
						}
					}
				}
			});

			if (similarityVals.size() == 0)
				return;
			Collections.sort(similarityVals, Comperators.EntryDoubleValueComp);
			int minHeight = Integer.MAX_VALUE;
			int maxHeight = 0;
			KPointCollection bestHeights = heightClusters.get(similarityVals.get(0).getKey());
			for (int i = 0; i < bestHeights.size(); i++)
			{
				if (bestHeights.get(i).getY() < minHeight)
					minHeight = (int) bestHeights.get(i).getY();
				if (bestHeights.get(i).getY() > maxHeight)
					maxHeight = (int) bestHeights.get(i).getY();
			}

			minHeight = minHeight - (int) ((float) minHeight * 0.03f);
			maxHeight = maxHeight + (int) ((float) maxHeight * 0.03f);

			for (List<? extends Rect> rects : clusters.values())
			{
				for (int i = rects.size() - 1; i >= 0; i--)
					if (rects.get(i).height() < minHeight || rects.get(i).height() > maxHeight)
						rects.remove(i);
			}
		}
		catch (ClusteringException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void evaluateClusterPositions(int clusterSizeMinVal)
	{
		List<Rect> newClusterkeys = new ArrayList<Rect>();
		List<Integer> xPosEvalDistances = new ArrayList<Integer>();
		int first = -1;
		int second = -1;
		int firstPos = -1;
		int firsthelp = -1;
		float last = 0;
		float next = 0;
		for (int i = clusterKeys.size() - 1; i >= 0; i--)
		{
			if (clusters.get(clusterKeys.get(i)).size() < 2)
			{
				clusterKeys.remove(i);
				continue;
			}
			if (i > 0)
				next = clusters.get(clusterKeys.get(i - 1)).size();
			else
				next = 0;
			float minVal = 0;
			if (next > 0 && last > 0)
				minVal = (next + last) / 6;
			if (clusters.get(clusterKeys.get(i)).size() < minVal)
			{
				clusterKeys.remove(i);
				firstPos--;
				continue;
			}
			last = clusters.get(clusterKeys.get(i)).size();
			if (clusters.get(clusterKeys.get(i)).size() >= clusterSizeMinVal) // not!!
			{
				second = first;
				first = clusterKeys.get(i).x();
				if (second >= 0)
				{
					xPosEvalDistances.add(second - first);
					firstPos = i;
				}
			}
		}

		float medianDistance = 0;
		Collections.sort(xPosEvalDistances);
		if (xPosEvalDistances.size() > 1)
		{
			if (xPosEvalDistances.size() % 2 == 0)
				medianDistance = (float) ((xPosEvalDistances.get((int) ((float) xPosEvalDistances.size() / 2)) + xPosEvalDistances
						.get((int) ((float) xPosEvalDistances.size() / 2) - 1)) / 2);
			else
				medianDistance = (float) (xPosEvalDistances.get((int) ((float) xPosEvalDistances.size() / 2)));
		}
		else
			return;
		int originalSize = clusterKeys.size();
		boolean noChange = false;
		first = -1;
		// TODO
		if (firstPos >= clusterKeys.size())
			firstPos = 0;
		second = clusterKeys.get(firstPos).x();
		for (int i = 0; i < originalSize; i++)
		{
			if (i > firstPos && (!noChange || first < 0)) // not!!
			{
				first = second;
			}
			else if (i <= firstPos)
				first = clusterKeys.get(i).x();
			if (i > firstPos)
				second = clusterKeys.get(i).x();
			firsthelp = i;

			if (first >= 0)// && clusters.get(clusterKeys.get(i)).size() >
							// clusterSizeMinVal/2) //not!!
			{
				float[] dist = new float[5];
				dist[1] = second - first;
				dist[2] = ((float) dist[1] / 2f);
				dist[3] = ((float) dist[1] / 3f);
				dist[4] = ((float) dist[1] / 4f);

				noChange = true;
				for (int j = 1; j < 5; j++)
				{
					if (dist[j] > (medianDistance) - (medianDistance * 0.2) && dist[j] < (medianDistance) + (medianDistance * 0.5))
					{
						if (firstPos >= 0)
						{
							if (!newClusterkeys.contains(clusterKeys.get(firstPos))) // not!!
								newClusterkeys.add(clusterKeys.get(firstPos));
						}

						newClusterkeys.add(clusterKeys.get(i));
						if (i > firstPos)
						{
							firstPos = firsthelp;
							noChange = false;

							if (j > 1 && i >= firstPos)
							{
								int count = 1;
								for (int h = j; h > 1; h--)
								{
									boolean inserted = false;
									while (!newClusterkeys.contains(clusterKeys.get(i - count))) // not!!
									{
										int d = clusterKeys.get(i).x() - clusterKeys.get(i - count).x();
										if (d > (medianDistance) - (medianDistance * 0.2) && d < (medianDistance) + (medianDistance * 0.5))
										{
											newClusterkeys.add(clusterKeys.get(i - count));
											inserted = true;
											break;
										}
										count++;
									}
									if (!inserted) // not!
										newClusterkeys.add(new Rect(first + (int) dist[h], 0, 0, 0));
								}
							}
						}
						break;
					}
				}
			}
			else
				noChange = true;
		}
		clusterKeys = newClusterkeys;
	}

	private void convertToBinaryImages(List<Mat> digits) throws FrameNotFoundException
	{
		for (int i = 0; i < digits.size(); i++)
		{
			if (digits.get(i) == null)
				continue;

			Mat binaryImage = digits.get(i);
			binaryImage = ImageStatics.GetBinaryImage(binaryImage, ImageStatics.BINARY_IMAGE_THRESHOLD, backGround);
			digits.set(i, binaryImage);
		}
	}

	// private Rect getDigitBoundingBox(Mat bitImage) throws
	// FrameNotFoundException
	// {
	// Rect frame = new Rect();
	// List<CvRect> boxes = ImageStatics.getBoundingBoxesOfContoures(bitImage,
	// this.backGround, 3, 5);
	// deleteOuterMostBox(boxes);
	// boxes = ImageStatics.DeleteOverLappingBoundingBoxes(boxes);
	// return getFinalBoxFrame(bitImage, frame, boxes);
	// }
	//
	private void deleteOuterMostBox(List<Rect> boxes)
	{
		for (int j = boxes.size() - 1; j >= 0; j--)
		{
			if (boxes.get(j).x() < 3 || boxes.get(j).y() < 3)
				boxes.remove(j);
		}
	}

	private Rect getFinalBoxFrame(Mat bitImage, Rect frame, List<Rect> boxes)
	{
		for (int j = boxes.size() - 1; j >= 0; j--)
		{
			Rect help = boxes.get(j);
			if ((float) bitImage.size().width() / 1.5 > help.width() || (float) bitImage.size().height() / 1.5 > help.height()) // delete
																																// small,
																																// independent
																																// box
				boxes.remove(j);
			if (help.width() * help.height() > frame.width() * frame.height())
				frame = help;
		}
		return frame;
	}

	private void scaleBinaryImageList(List<List<Mat>> digits)
	{
		for (int i = 0; i < digits.size(); i++)
		{
			if (digits.get(i) != null)
			{
				List<Mat> digitList = digits.get(i);
				scaleBinaryImages(digitList);
			}
		}
	}

	private void scaleBinaryImages(List<Mat> digitList)
	{

		for (int j = 0; j < digitList.size(); j++)
		{
			if (digitList.get(j) != null)
			{
				Mat kk = new Mat(new Size(ImageStatics.DIGIT_STORE_SIZE_WIDTH, ImageStatics.DIGIT_STORE_SIZE_HEIGHT), CV_8UC1);
				// TODO check this
				resize(digitList.get(j), kk, kk.size(), 0, 0, 1);

				kk = ImageStatics.GetBinaryImage(kk, ImageStatics.BINARY_IMAGE_THRESHOLD, backGround);
				digitList.set(j, kk);
			}
		}
	}
}
