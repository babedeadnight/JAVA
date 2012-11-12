import java.util.Calendar;
 
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import MatlabDraw.MatlabDraw;

public class HelloWorld {
	static myCalculator mc = null;
	static MatlabDraw md = null;
	static double begin;
	private static myGUI mygui_;
	public static void main(String args[]) throws MWException {
		//mc = new myCalculator();
		//try {md = new MatlabDraw();} catch (MWException e) {}
		// opt: zStart,zScale,zStep,Radius,RInterstep
		// mc.DataInit(new double[]{0,10,2,20,1});
		// testGosseCenter(32, 1);
		//testGetZPosition();
		testGUI();
	}
	public static void testGUI(){
		mygui_ = new myGUI();
		mygui_.GUIInitialization();
		double a[] = new double[1000];
		for (int i=0;i<1000;i++){
			a[i] = i;
			mygui_.dataSeries_.add(i, Math.sin(i));
			mygui_.cosSeries.add(i, Math.cos(i));
		 	try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		 
	}
	public static void testGetZPosition() throws MWException {
		Object[] ret = null;
		Object[] zret = null;
		int zlen = 10;
		int[] roi_ = new int[] { 0, 0, 200, 250 };
		// [70.0, 0.5, 32.0, 5.0, 512.0, 512.0, 5.0, -2.0, 4.0, 0.1]
		// opt_
		// :radius,rInterStep,bitDepth,halfQuadWidth,imgWidth,imgHeight,zcalTimes_,zStart,zScale,zStep
		double[] opt_ = new double[] { 40, 0.5, 32, 2, 200, 250, 1, 1, zlen, 1 };
		mc.DataInit(opt_);

		for (int i = 1; i < zlen; i++) {
			ret = md.getImage(1, i, 1);
			MWNumericArray out_img = (MWNumericArray) ret[0];
			float[] img = out_img.getFloatData();
			ret = mc.Calibration(img, roi_, i - 1);
			// md.draw(((double[])(ret[2])),new double[]{95,200,1.5,3},1);
			System.out.print("calibrating:" + i + "/" + zlen + "\r\n");
			System.out.print("xPos#" + ((double[]) (ret[0]))[0] + "\t");
			System.out.print("yPos#" + ((double[]) (ret[0]))[1] + "\t");
			System.out.print("zPos#" + ((double[]) (ret[0]))[2] + "\t");
			System.out.print("ERR_CODE#"
					+ mc.getErrCode((int) (((double[]) (ret[1]))[0])) + "\t");
			System.out.print("Cost Time#" + ((double[]) (ret[1]))[1] + "\r\n");
		}
		ret = md.getImage(1, 5, 1);
		MWNumericArray out_img = (MWNumericArray) ret[0];
		mc.SetBitDepth(16);
		double[] img = out_img.getDoubleData();
		short[][] img_ =new short[250][200];
		for(int i=0;i<250;i++){
			for(int j=0;j<200;j++){
				img_[i][j] =(short)img[j*250+i] ;
			}
		} 

		/*short[] img_ =new short[200*250];
		for(int i=0;i<200;i++){
			for(int j=0;j<250;j++){
				img_[i*250+j] =(short)img[i*250+j] ;
			}
		}*/

		int len = 100000000;
		for (int i = 1; i < len; i++) {
			start();
			zret = mc.GetZPosition(img_, roi_);
			end();
			System.out.print("Geting ZPostion:" + i + "/" + 100000000 + "\r\n");
			System.out.print("xPos#" + ((double[]) (zret[0]))[0] + "\t");
			System.out.print("yPos#" + ((double[]) (zret[0]))[1] + "\t");
			System.out.print("zPos#" + ((double[]) (zret[0]))[2] + "\t");
			System.out.print("ERR_CODE#"
					+ mc.getErrCode((int) (((double[]) (zret[1]))[0])) + "\t");
			System.out.print("Cost Time#" + ((double[]) (zret[1]))[1] + "\r\n");
		}

	}

	public static void testCalibration(int bitDepth, int mode)
	throws MWException {
		int w = 300;
		int h = 300;
		int[] roi_ = new int[] { 0, 0, 300, 300 };

		Object[] ret = null;
		if (bitDepth == 32) {
			double[] opt_ = new double[] { 20, 0.5, 32, 2, 300, 300 };
			if (mode == 0) {
				float[][] image_ = new float[h][w];
				image_[101][100] = (float) 1;
				ret = mc.Calibration(image_, roi_, 0);
			}
			if (mode == 1) {
				float[] image = new float[300 * 300];
				image[150 * w + 150] = (float) 1;
				ret = mc.Calibration(image, roi_, 0);
			}
		}
		if (bitDepth == 16) {
			double[] opt_ = new double[] { 20, 0.5, 16, 2, 300, 300 };
			if (mode == 0) {
				short[][] image_ = new short[h][w];
				image_[100][100] = (short) 1;
				ret = mc.Calibration(image_, roi_, 0);
			}
			if (mode == 1) {
				short[] image = new short[300 * 300];
				image[150 * w + 150] = (short) 1;
				ret = mc.Calibration(image, roi_, 0);
			}

		}

		// md.draw((double[])ret[0],new double[]{100,200,0,100},1);
		for (int i = 0; i < ((double[]) ret[1]).length; i++) {
			System.out.print(((double[]) ret[1])[i] + "\r\n");
		}
		System.out.print("ERR_CODE:\t"
				+ mc.getErrCode((int) ((double[]) ret[2])[0]) + "\r\n");
		System.out.print("Cost time:\t" + ((double[]) ret[2])[1] + "\r\n");

	}



	static long now() {
		Calendar theCa;
		theCa = new GregorianCalendar();
		return theCa.getTimeInMillis();
	}

	public static void start() {
		begin = now();
	}

	public static void end() {
		long end = now();
		System.out.print("\r\nTime Cost\r\n");
		System.out.print(end - begin);
		System.out.print("\r\n\r\n\r\n");
	}

	public static void testGosseCenter(int bitDepth, int mode)
	throws MWException {
		Object ret[] = null;
		int zlen = 10;
		int[] roi_ = new int[] { 0, 0, 200, 250 };
		// opt_
		// :radius,rInterStep,bitDepth,halfQuadWidth,imgWidth,imgHeight,zcalTimes_,zStart,zScale,zStep
		double[] opt_ = new double[] { 40, 0.5, 32, 2, 200, 250, 1, 1, zlen, 1 };

		if (bitDepth == 32) {
			opt_[2] = (double) 32;
			mc.DataInit(opt_);

			ret = md.getImage(1, 15, 1);
			MWNumericArray out_img = (MWNumericArray) ret[0];
			float[] img = out_img.getFloatData();
			short[] img_ =new short[200*250];
			for(int i=0;i<200;i++){
				for(int j=0;j<250;j++){
					img_[i*250+j] =(short)img[i*250+j] ;
				}
			}
			mc.SetBitDepth(16);
			int debuglen = 1000000;
			for (int j = 0; j < debuglen; j++) {
				ret = mc.GosseCenter(img_, roi_);
				double[] pos = (double[]) ret[0];
				System.out.print("Debuging:" + j + "/" + debuglen + "\tXpos#"
						+ pos[0] + "\tYpos:" + pos[1]);
				System.out.print("\tERR_CODE#"
						+ mc.getErrCode((int) ((double[]) ret[1])[0]));
				System.out.print("\tCost time#" + ((double[]) ret[1])[1]
				                                                      + "\r\n");
			}
		}

	}

}