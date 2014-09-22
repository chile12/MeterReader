package ccc.java.stringalignement;

import java.security.InvalidParameterException;
import java.util.List;

import ccc.java.digitextractor.data.MatchingCharacter;
import ccc.java.digitextractor.exceptions.NotProcessedException;

public class NeedlemanWunsch
{
	private boolean processed = false;
	private char[] mSeqA;
	private char[] mSeqB;
	private int[][] mD;
	private int mScore;
	private int maxVal = 0;
	private String mAlignmentSeqA = "";
	private String mAlignmentSeqB = "";
	private int anchorShiftedRight = 0;
	private int anchorShiftedLeft = 0;
	private int compSeqShiftedRight = 0;
	private int compSeqShiftedLeft = 0;

	private List<MatchingCharacter> aRow;
	private List<MatchingCharacter> bRow;

	public NeedlemanWunsch(String a, String b)
	{
		this.init(a.toCharArray(), b.toCharArray());
	}

	public NeedlemanWunsch(List<MatchingCharacter> a, List<MatchingCharacter> b)
	{
		aRow = a;
		bRow = b;
		this.init(MatchingCharacter.extractString(a).toCharArray(), MatchingCharacter.extractString(b).toCharArray());
	}

	void init(char[] seqA, char[] seqB)
	{
		mSeqA = seqA;
		mSeqB = seqB;
		mD = new int[mSeqA.length + 1][mSeqB.length + 1];
		for (int i = 0; i <= mSeqA.length; i++)
		{
			for (int j = 0; j <= mSeqB.length; j++)
			{
				if (i == 0)
				{
					mD[i][j] = -j;
				}
				else if (j == 0)
				{
					mD[i][j] = -i;
				}
				else
				{
					mD[i][j] = 0;
				}
			}
		}
	}

	public void process()
	{
		for (int i = 1; i <= mSeqA.length; i++)
		{
			for (int j = 1; j <= mSeqB.length; j++)
			{
				int scoreDiag = mD[i - 1][j - 1] + weight(i, j);
				int scoreLeft = mD[i][j - 1] - 1;
				int scoreUp = mD[i - 1][j] - 1;
				mD[i][j] = Math.max(Math.max(scoreDiag, scoreLeft), scoreUp);
				if (maxVal < mD[i][j])
					maxVal = mD[i][j];
			}
		}
	}

	public void backtrack()
	{
		int i = mSeqA.length;
		int j = mSeqB.length;
		mScore = mD[i][j];
		while (i > 0 || j > 0)
		{

			if (j == 0)
			{
				mAlignmentSeqA += mSeqA[i - 1];
				mAlignmentSeqB += "-";
				compSeqShiftedRight++;
				i--;
				continue;
			}
			if (i == 0)
			{
				mAlignmentSeqB += mSeqB[j - 1];
				mAlignmentSeqA += "-";
				anchorShiftedRight++;
				j--;
				continue;
			}
			if (mD[i][j] == mD[i - 1][j - 1] + weight(i, j))
			{

				mAlignmentSeqA += mSeqA[i - 1];
				mAlignmentSeqB += mSeqB[j - 1];
				i--;
				j--;
				continue;
			}
			else if (mD[i][j] == mD[i][j - 1] - 1)
			{
				mAlignmentSeqA = "-" + mAlignmentSeqA;
				mAlignmentSeqB += mSeqB[j - 1];
				anchorShiftedLeft++;
				j--;
				continue;
			}
			else
			{
				mAlignmentSeqA += mSeqA[i - 1];
				mAlignmentSeqB = "-" + mAlignmentSeqB;
				compSeqShiftedLeft++;
				i--;
				continue;
			}
		}
		mAlignmentSeqA = new StringBuffer(mAlignmentSeqA).reverse().toString();
		mAlignmentSeqB = new StringBuffer(mAlignmentSeqB).reverse().toString();

		processed = true;
	}

	private int weight(int i, int j)
	{
		if (mSeqA[i - 1] == '-' || mSeqB[j - 1] == '-')
			return 0;
		if (mSeqA[i - 1] == mSeqB[j - 1])
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}

	public int getMatchCount()
	{
		if (processed)
		{
			int count = 0;
			for (int i = 0; i < mAlignmentSeqA.length(); i++)
			{
				if (mAlignmentSeqA.charAt(i) == mAlignmentSeqB.charAt(i))
					count++;
			}
			return count;
		}
		else
			return -1;
	}

	public double getRowMatchValue()
	{
		double ret = 0d;
		if (processed && aRow != null && bRow != null)
		{
			int aRight = anchorShiftedRight;
			int bRight = compSeqShiftedRight;
			for (int i = 0; i < Math.min(aRow.size(), bRow.size()); i++)
			{
				if (aRow.get(i) == null)
					bRight++;
				else
					break;
				if (bRow.get(i) == null)
					aRight++;
				else
				{
					bRight--;
					break;
				}
			}
			for (int i = Math.max(bRight, aRight); i < Math.min(aRow.size() - aRight, bRow.size() - bRight); i++)
			{
				if (aRow.get(i - aRight) != null && bRow.get(i - bRight) != null
						&& aRow.get(i - aRight).getCharacter() == bRow.get(i - bRight).getCharacter())
				{
					ret += (100 - aRow.get(i - aRight).getPrecision()) * (100 - bRow.get(i - bRight).getPrecision());
				}
			}
		}
		return ret;
	}

	// /alines both strings in a given context (as int-matrix rows) by there
	// char values
	// /usually in the middle of a sufficiently spaced int-array (row)
	// (arraylength)
	// /startPosition the start position of the anchor row (aRow) in the matrix
	// row, returns char as int and precision as position array
	public int[][] getAlignedCompRow(int arrayLength, int startPosition) throws NotProcessedException, InvalidParameterException
	{
		if (arrayLength < mAlignmentSeqB.length())
			throw new InvalidParameterException("array shorter than compareRow");
		if (processed)
		{
			int compRowStart = startPosition - anchorShiftedRight + compSeqShiftedRight;
			int bRowStart = 0;
			for (int i = 0; i < bRow.size(); i++)
				if (bRow.get(i) != null)
				{
					bRowStart = i;
					break;
				}
			int[][] aligned = new int[arrayLength][2];

			for (int i = compRowStart; i < compRowStart + mSeqB.length; i++)
			{
				aligned[i][0] = mSeqB[i - compRowStart];
				if (bRow != null && bRow.get(bRowStart + i - compRowStart) != null)
					aligned[i][1] = (int) (bRow.get(bRowStart + i - compRowStart).getPrecision());
			}

			return aligned;
		}
		throw new NotProcessedException("NeedlemanWunsch not jet processed");
	}

	public String getmAlignmentSeqA()
	{
		return mAlignmentSeqA;
	}

	public String getmAlignmentSeqB()
	{
		return mAlignmentSeqB;
	}

	public List<MatchingCharacter> getaRow()
	{
		return aRow;
	}

	public List<MatchingCharacter> getbRow()
	{
		return bRow;
	}

	public int getAnchorShiftedRight()
	{
		return anchorShiftedRight;
	}

	public int getAnchorShiftedLeft()
	{
		return anchorShiftedLeft;
	}

	public int getCompSeqShiftedRight()
	{
		return compSeqShiftedRight;
	}

	public int getCompSeqShiftedLeft()
	{
		return compSeqShiftedLeft;
	}

	// /DEBUG
	void printMatrix()
	{
		System.out.println("D =");
		for (int i = 0; i < mSeqA.length + 1; i++)
		{
			for (int j = 0; j < mSeqB.length + 1; j++)
			{
				System.out.print(String.format("%4d ", mD[i][j]));
			}
			System.out.println();
		}
		System.out.println();
	}

	void printScoreAndAlignments()
	{
		System.out.println("MaxVal: " + maxVal);
		System.out.println("Score: " + mScore);
		System.out.println("anchorShiftedRight: " + anchorShiftedRight);
		System.out.println("anchorShiftedLeft: " + anchorShiftedLeft);
		System.out.println("compSeqShiftedRight: " + compSeqShiftedRight);
		System.out.println("compSeqShiftedLeft: " + compSeqShiftedLeft);
		System.out.println("Sequence A: " + mAlignmentSeqA);
		System.out.println("Sequence B: " + mAlignmentSeqB);
		System.out.println("RowMatchValue: " + getRowMatchValue());
		System.out.println();
	}

	public static void main(String[] args)
	{

		NeedlemanWunsch nw = new NeedlemanWunsch("7, 1, 6, 6, 8, 7, 4, 5", "8, 8, 8, 7, 4, 5, 1");
		nw.process();
		nw.backtrack();

		nw.printMatrix();
		nw.printScoreAndAlignments();
	}
}