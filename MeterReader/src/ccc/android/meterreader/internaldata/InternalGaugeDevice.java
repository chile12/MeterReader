package ccc.android.meterreader.internaldata;

import java.util.ArrayList;

import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.types.GaugeDevice;
import ccc.android.meterdata.types.GaugeDeviceDigit;
import ccc.java.digitextractor.ImageStatics;
import ccc.java.digitextractor.exceptions.MatrixMissmatchException;
import ccc.java.simpleocr.BinaryPattern;
import ccc.java.simpleocr.BinaryPatternContainer;

public class InternalGaugeDevice extends GaugeDevice
{
	private BinaryPatternContainer binaryPatterns = new BinaryPatternContainer();
	public InternalGaugeDevice(GaugeDevice device)
	{
		this.setBackGround(device.getBackGround());
		this.setComment(device.getComment());
		this.setDecimalPlaces(device.getDecimalPlaces());
		this.setDigitCount(device.getDigitCount());
		this.setGaugeDeviceId(device.getGaugeDeviceId());
		this.setManufacturer(device.getManufacturer());
		this.setSerialNumber(device.getSerialNumber());
		this.setUtcInstallation(device.getUtcInstallation());
		this.setValueOffset(device.getValueOffset());
		this.setDigitPatterns(device.getDigitPatterns());
	}

	
	@Override
	public GaugeDeviceDigitList getDigitPatterns() {
		return null;
	}
	@Override
	public void setDigitPatterns(GaugeDeviceDigitList digitPatterns) {
		if(digitPatterns == null)
			return;
		for(GaugeDeviceDigit digit : digitPatterns.getDigitList())
		{
			Character chr = String.valueOf(digit.getDigit()).charAt(0);
			if(binaryPatterns.get(chr)==null)
				binaryPatterns.put(chr, new ArrayList<BinaryPattern>());
			try {
				binaryPatterns.get(chr).add(new BinaryPattern(chr, ImageStatics.DIGIT_STORE_SIZE_WIDTH, ImageStatics.DIGIT_STORE_SIZE_HEIGHT, digit.getBinary()));
			} catch (MatrixMissmatchException e) {
				e.printStackTrace();
			}catch (Exception cnf)
			{
				if(cnf.getMessage() == null || !cnf.getMessage().contains("java.awt")) //not!!
					cnf.getMessage();
			}
		}
	}
	
	public BinaryPatternContainer getBinaryPatterns() {
		return binaryPatterns;
	}
	public void setBinaryPatterns(BinaryPatternContainer binaryPatterns) {
		this.binaryPatterns = binaryPatterns;
	}
}
