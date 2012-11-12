import ij.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class myGUI {
	// main instance
	private ZIndexMeasure Maininstance_ = null;
	private static myGUI instance_;
	// GUI
	private JFrame f_ = new JFrame("ZIndexMeasure");
	private JTextArea record = new JTextArea(50, 40);
	private JTextField msg = new JTextField(36);
	public JTextField msg1 = new JTextField(36);
	private JTextField Raduis = new JTextField(1);
	private JTextField Scale = new JTextField(1);
	private JTextField Step = new JTextField(1);
	private JTextField RInterp = new JTextField(4);
	private JTextField halfTrackWindow = new JTextField(1);
	private JTextField getClaTimes = new JTextField(1);
	private JTextField halfCorrWindow = new JTextField(1);
	private JTextField frameCalcForce = new JTextField(1);
	private JTextField frame2Acq = new JTextField(1);
	private JTextField TimeIntervals = new JTextField(1);
	private JTextField StoragePath = new JTextField(1);
	private JButton DebugSet = new JButton("DebugSet");
	private JButton DebugRun = new JButton("DebugRun");

	public JCheckBox checkBoxX = new JCheckBox("X");
	public JCheckBox checkBoxY = new JCheckBox("Y");
	public JCheckBox checkBoxZ = new JCheckBox("Z");
	public JCheckBox checkBoxTrackRaw = new JCheckBox("Raw");
	public JCheckBox checkBoxTrackResult = new JCheckBox("Result");

	private XYSeriesCollection dataset_;

	public XYSeries dataSeries_;

	public int MAX_LEN = 200000;
	// opt:Radius,RInterstep,bitDepth,halfQuadWidth,imgWidth,imgHeight,(zX),zcalTimes_,zStart,zScale,zStep
	private double[] calcOpt_ = null;
	private int Radius_ = 70;
	private double RInterstep_ = 0.5;
	private double bitDepth_ = 32;
	private int halfQuadWindow_ = 2;
	private int imgwidth_ = 640;
	private int imgheight_ = 480;
	private int zcalTimes_ = 1;
	private double zStart_ = 0;
	
	private double zScale_ = 4;
	private double zStep_ = 0.1;

	public double[] fOpt_ = null;// calculate force
	// ROI
	private int[] calcRoi_ = null;
	private int roix_ = 0;
	private int roiy_ = 0;
	private int roiwidth_ = 40;
	private int roiheight_ = 40;
	// other
	public int frameCalcForce_ = 1000;
	public int sleeptime_ = 20;
	public double[] calcForce = null;
	public double[] calPos_;
	public int frame2Acq_ = 100000;
	public int TimeIntervals_ = 5;
	public String StoragePath_ = "E:/Users/n~daguan/AcquisitionData";

	public double[][] resultSave = null;
	public Rectangle roi_rectangle;
	private int isPause = 0;
	// temp
	private long begin;
	public JButton Pause;
	public JButton Live;

	private JPanel createChartPanel() {
		dataSeries_ = new XYSeries("Z");
		dataSeries_.setMaximumItemCount(200);

		dataset_ = new XYSeriesCollection();
		dataset_.addSeries(dataSeries_);

		JFreeChart chart = ChartFactory.createXYLineChart("ZIndexMeasure",
				"X-time", "Y-zPosition", dataset_, PlotOrientation.VERTICAL,
				true, true, false);
		ChartPanel panel = new ChartPanel(chart, true);
		// (new DataGenerator(100)).start();
		return panel;
	}

	public int[] getROI() {
		return calcRoi_;
	}

	public double[] getOpt() {
		return calcOpt_;
	}

	public static myGUI getInstance() {
		return instance_;
	}

	public void reSetROI(int CenterX, int CenterY) {
		// get ROI from current image and reset it to fit the ball radius
		int ROIBounder = 10;
		roi_rectangle.width = (int) ((ROIBounder + Radius_) * 2);
		roi_rectangle.height = (int) ((ROIBounder + Radius_) * 2);
		roi_rectangle.x = (int) (CenterX - roi_rectangle.width / 2);
		roi_rectangle.y = (int) (CenterY - roi_rectangle.height / 2);

		if ((roi_rectangle.x <= 5 || roi_rectangle.y <= 5)
				|| ((roi_rectangle.x + Radius_ * 2 + 5 >= calcOpt_[4]) || (roi_rectangle.y
						+ Radius_ * 2 + 5 >= calcOpt_[5]))) {
			log("YOU BALL IS OUT OF CONTROL,I AM TIRE OF FOLLOWING....");
			return;
		}
		//WindowManager.getCurrentImage().setRoi(roi_rectangle);

		calcRoi_[0] = roi_rectangle.x;
		calcRoi_[1] = roi_rectangle.y;
		calcRoi_[2] = roi_rectangle.width;
		calcRoi_[3] = roi_rectangle.height;
	}

	public myGUI() {
		instance_ = this;
		Maininstance_ = ZIndexMeasure.getInstance();
		resultSave = new double[MAX_LEN][5];
		calcOpt_ = new double[] { Radius_, RInterstep_, bitDepth_,
				halfQuadWindow_, imgwidth_, imgheight_, zcalTimes_, zStart_,
				zScale_, zStep_ };
		calcRoi_ = new int[] { roix_, roiy_, roiwidth_, roiheight_ };
		roi_rectangle = new Rectangle();// default ROI

		record.setText("Welcome");
		Raduis.setText(String.format("%d", Radius_));
		Scale.setText(String.format("%f", zScale_));
		Step.setText(String.format("%f", zStep_));
		RInterp.setText(String.format("%f", RInterstep_));
		halfTrackWindow.setText(String.format("%d", 0));
		getClaTimes.setText(String.format("%d", 1));
		halfCorrWindow.setText(String.format("%d", halfQuadWindow_));
		frameCalcForce.setText(String.format("%d", frameCalcForce_));

		frame2Acq.setText(String.format("%d", frame2Acq_));
		TimeIntervals.setText(String.format("%d", TimeIntervals_));
		StoragePath.setText(String.format("%s", StoragePath_));

		checkBoxX.setSelected(false);
		checkBoxY.setSelected(false);
		checkBoxZ.setSelected(false);
		checkBoxTrackRaw.setSelected(false);

		msg.setText(String.format("Radius =%d  ,Scale = %f, Step = %f",
				Radius_, zScale_, zStep_));
		msg1
				.setText(String.format(
						"index =%d  ,xpos = %f, ypos = %f,zpos = %f", 0, 0.0,
						0.0, 0.0));

	}

	private void AdvanceSet() {
		// get&set input from GUI
		RInterstep_ = Double.parseDouble(RInterp.getText());
		halfQuadWindow_ = Integer.parseInt(halfCorrWindow.getText());
		zcalTimes_ = Integer.parseInt(getClaTimes.getText());
		reSetOpt();
	}

	private void SetScale() {
		log("SetScale Start......");
		if (WindowManager.getCurrentImage() == null) {
			log("please open an image,use the live button");
			return;
		}
		if (WindowManager.getCurrentImage().getRoi() == null) {
			log("please set ROI,the tool is locate in the imagej main frame");
			return;
		}
		// reset ROI
		roi_rectangle = WindowManager.getCurrentImage().getRoi().getBounds();
		int CenterX = roi_rectangle.x + roi_rectangle.width / 2;
		int CenterY = roi_rectangle.y + roi_rectangle.height / 2;
		// get input from GUI
		Radius_ = Integer.parseInt(Raduis.getText());
		zScale_ = Double.parseDouble(Scale.getText());
		zStep_ = Double.parseDouble(Step.getText());
		frameCalcForce_ = Integer.parseInt(frameCalcForce.getText());

		calcForce = new double[frameCalcForce_];
		fOpt_ = new double[] { 16, 300, 50, frameCalcForce_ };//uM K nM

		zStart_ = Maininstance_.currzpos_ - zScale_ / 2;
		imgwidth_ = WindowManager.getCurrentImage().getProcessor().getWidth();
		imgheight_ = WindowManager.getCurrentImage().getProcessor().getHeight();

		frame2Acq_ = Integer.parseInt(frame2Acq.getText());
		TimeIntervals_ = Integer.parseInt(TimeIntervals.getText());
		StoragePath_ = StoragePath.getText();

		reSetOpt();
		reSetROI(CenterX, CenterY);
		setCalProfile();
		Maininstance_.isSetScale = 1;
		Maininstance_.isCalibration = 0;

	}

	private void setCalProfile() {
		int calProfiley = (int) (zScale_ / (zStep_));
		calPos_ = new double[calProfiley];
		for (int i = 0; i < calProfiley; i++) {
			calPos_[i] = Maininstance_.currzpos_ - zScale_ / 2 + i * zStep_;
		}

		Maininstance_.mCalc.DataInit(getOpt());
		Maininstance_.isSetScale = 1;
		msg.setText(String.format("Radius =%d  ,Scale = %f, Step = %f",
				Radius_, zScale_, zStep_));
		log("Scale seted\t");
		log(String.format("from   %f  umto  %f um,by %f um\r\n", -zScale_ / 2,
				zScale_ / 2, zStep_));
	}

	// opt_
	// :radius,rInterStep,bitDepth,halfQuadWidth,imgWidth,imgHeight,zcalTimes_,zStart,zScale,zStep
	private void reSetOpt() {
		calcOpt_[0] = Radius_;
		calcOpt_[1] = RInterstep_;
		calcOpt_[2] = bitDepth_;
		calcOpt_[3] = halfQuadWindow_;
		calcOpt_[4] = imgwidth_;
		calcOpt_[5] = imgheight_;
		calcOpt_[6] = zcalTimes_;
		calcOpt_[7] = zStart_;
		calcOpt_[8] = zScale_;
		calcOpt_[9] = zStep_;

	}

	public void GUIInitialization() {

		// Add GUI DATA
		JSeparator s1 = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator s2 = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator s3 = new JSeparator(JSeparator.HORIZONTAL);

		// top
		JPanel top = new JPanel(new BorderLayout());
		Box Top = Box.createVerticalBox();
		JScrollPane taJsp = new JScrollPane(createChartPanel());
		taJsp.setPreferredSize(new Dimension(800, 600));
		Top.add(taJsp);
		Color bg1 = new Color(18, 25, 19);
		msg1.setBackground(bg1);
		msg1.setEnabled(false);
		Top.add(msg1);
		// center
		JPanel center = new JPanel(new BorderLayout());
		Color bg = new Color(13, 15, 19);
		msg.setBackground(bg);
		msg.setEnabled(false);
		Top.add(msg);
		
		
		

		// bottom
		JPanel bottom = new JPanel(new BorderLayout());
		record.setText("welcome .....");
		record.setLineWrap(true);
		Box Bottom = Box.createVerticalBox();
		Bottom.add(record);
		JScrollPane Jsp = new JScrollPane(Bottom);
		Jsp.setPreferredSize(new Dimension(500, 150));

		Top.add(Jsp);
		top.add(Top);
		// middle = top + center +bottom
		JPanel middle = new JPanel(new BorderLayout());
		middle.add(top, BorderLayout.NORTH);
		middle.add(center, BorderLayout.CENTER);
		middle.add(bottom, BorderLayout.SOUTH);

		// left
		JPanel left = new JPanel(new BorderLayout());
		// left Top
		JPanel lefttop = new JPanel(new BorderLayout());
		Box LeftTop = Box.createVerticalBox();
		JLabel lable1 = new JLabel("Initial");
		LeftTop.add(lable1);
		lefttop.add(LeftTop, BorderLayout.NORTH);
		lefttop.add(s1, BorderLayout.SOUTH);
		// left center
		Box LeftCenter = Box.createVerticalBox();
		JLabel lable2 = new JLabel("R/pixel");
		LeftCenter.add(lable2);
		LeftCenter.add(Raduis);
		JLabel lable3 = new JLabel("Len/um");
		LeftCenter.add(lable3);
		LeftCenter.add(Scale);
		JLabel lable4 = new JLabel("Step/um");
		LeftCenter.add(lable4);
		LeftCenter.add(Step);

		JLabel lable5 = new JLabel("frame2Acq/f");
		LeftCenter.add(lable5);
		LeftCenter.add(frame2Acq);
		JLabel lable6 = new JLabel("TimeIntervals/ms");
		LeftCenter.add(lable6);
		LeftCenter.add(TimeIntervals);
		JLabel lable7 = new JLabel("StoragePath");
		LeftCenter.add(lable7);
		LeftCenter.add(StoragePath);
		JLabel lable8 = new JLabel("frameCalcForce");
		LeftCenter.add(lable8);
		LeftCenter.add(frameCalcForce);

		JPanel leftcenter = new JPanel(new BorderLayout());
		leftcenter.add(LeftCenter);
		// left Bottom
		JPanel leftbottom = new JPanel(new BorderLayout());
		JButton Set = new JButton("Set");
		JButton Calibration = new JButton("Calibration");
		JButton Start = new JButton("Start");
		Pause = new JButton("Pause");
		Live = new JButton("Live");
		JButton Stop = new JButton("Stop");
		JButton Save = new JButton("Save");

		Box LeftBottom = Box.createVerticalBox();
		LeftBottom.add(Live);
		LeftBottom.add(Set);
		LeftBottom.add(Calibration);
		LeftBottom.add(Start);
		LeftBottom.add(Pause);
		LeftBottom.add(Stop);
		LeftBottom.add(Save);
		leftbottom.add(LeftBottom);
		// left end
		left.add(lefttop, BorderLayout.NORTH);
		left.add(leftcenter, BorderLayout.CENTER);
		left.add(leftbottom, BorderLayout.SOUTH);
		// right

		JPanel right = new JPanel(new BorderLayout());
		// right Top
		JPanel righttop = new JPanel(new BorderLayout());
		Box RightTop = Box.createVerticalBox();
		JLabel lable05 = new JLabel("Debug");
		RightTop.add(lable05);
		righttop.add(RightTop, BorderLayout.NORTH);
		righttop.add(s2, BorderLayout.SOUTH);
		// right center
		Box RightCenter = Box.createVerticalBox();
		JLabel lable21 = new JLabel("RInterp");
		RightCenter.add(lable21);
		RightCenter.add(RInterp);
		JLabel lable22 = new JLabel("halfTrackWindow");
		RightCenter.add(lable22);
		RightCenter.add(halfTrackWindow);
		JLabel lable23 = new JLabel("getClaTimes/um");
		getClaTimes.setText("1");
		RightCenter.add(lable23);
		RightCenter.add(getClaTimes);
		JLabel lable24 = new JLabel("halfCorrWindow");
		RightCenter.add(lable24);
		RightCenter.add(halfCorrWindow);
		JLabel lable25 = new JLabel("DebugMode");

		JPanel debugmode = new JPanel();
		debugmode.add(checkBoxX);
		debugmode.add(checkBoxY);
		debugmode.add(checkBoxZ);
		JLabel lable33 = new JLabel("DataTrack");

		JPanel debugmode1 = new JPanel();
		debugmode1.add(checkBoxTrackRaw);
		debugmode1.add(checkBoxTrackResult);
		RightCenter.add(lable25);
		RightCenter.add(debugmode);
		RightCenter.add(s3);

		RightCenter.add(lable33);

		RightCenter.add(debugmode1);
		JPanel rightcenter = new JPanel(new BorderLayout());
		rightcenter.add(RightCenter);
		// right Bottom
		JPanel rightbottom = new JPanel();
		Box RightBottom = Box.createVerticalBox();
		RightBottom.add(DebugSet);
		RightBottom.add(DebugRun);
		rightbottom.add(RightBottom);

		right.add(righttop, BorderLayout.NORTH);
		right.add(rightcenter, BorderLayout.CENTER);
		right.add(rightbottom, BorderLayout.SOUTH);
		// right end

		righttop.add(RightTop, BorderLayout.NORTH);
		righttop.add(s2, BorderLayout.SOUTH);

		// right Bottom
		JPanel rightbottom1 = new JPanel();
		Box RightBottom1 = Box.createVerticalBox();
		rightbottom1.add(RightBottom1);

		// main frame
		Container con = f_.getContentPane();
		con.setLayout(new BorderLayout());
		con.add(left, BorderLayout.WEST);
		con.add(middle, BorderLayout.CENTER);
		// con.add(right, BorderLayout.EAST);

		f_.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		f_.pack();
		f_.setVisible(true);
		JFrame.setDefaultLookAndFeelDecorated(false);

		ActionListener menuListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				try {

					if (e.getActionCommand().equals("Start")) {
						log(String.format("Atempt to InstallCallback at  %s ",
								getTime()));
						
						frame2Acq_ = Integer.parseInt(frame2Acq.getText());
						TimeIntervals_ = Integer.parseInt(TimeIntervals.getText());
						StoragePath_ = StoragePath.getText();

						Maininstance_.InstallCallback();
					} else if (e.getActionCommand().equals("Stop")) {	
						if (!Maininstance_.gui_.getAcquisitionEngine()
								.isAcquisitionRunning()) {
							log("Acquisition is not running ,abort!");
							return;
						}
						else{
							log(String.format(
									"Atempt to UninstallCallback at  %s ",
									getTime()));
							Maininstance_.UninstallCallback();
							Maininstance_.UninstallCallback();
						    Maininstance_.gui_.getAcquisitionEngine().stop(true);
						}	
					} else if (e.getActionCommand().equals("Pause")
							|| e.getActionCommand().equals("Resume")) {
						
						if (!Maininstance_.gui_.getAcquisitionEngine()
								.isAcquisitionRunning()) {
							log("Acquisition is not running ,abort!");
							return;
						}

						if (isPause == 0) {
							Maininstance_.gui_.getAcquisitionEngine().setPause(
									true);
							Pause.setText("Resume");
							log(String.format("Atempt to Pause at  %s ", getTime()));
							isPause = 1;
						} else {
							Maininstance_.gui_.getAcquisitionEngine().setPause(
									false);
							Pause.setText("Pause");
							log(String.format("Atempt to Resume at  %s ", getTime()));
							isPause = 0;
						}
					} else if (e.getActionCommand().equals("Live")
							|| e.getActionCommand().equals("Stop Live")) {
						if (Maininstance_.gui_.getAcquisitionEngine()
								.isAcquisitionRunning()) {
							log("Acquistion is running,mission abort!");
							return;
						}
						if (Maininstance_.gui_.isLiveModeOn()) {
							Maininstance_.gui_.enableLiveMode(false);
							Live.setText("Live");
						} else {
							Live.setText("Stop Live");
							Maininstance_.gui_.enableLiveMode(true);
						}
					} else if (e.getActionCommand().equals("Save")) {
						log(String.format("Atempt to SaveFile at  %s ",
								getTime()));
						saveFile();
					} else if (e.getActionCommand().equals("Set")) {
						log(String.format("Atempt to SetScale at %s ",
								getTime()));
						SetScale();
					} else if (e.getActionCommand().equals("Calibration")) {
						log(String.format("Atempt to StartCalibration at %s ",
								getTime()));
						Maininstance_.StartCalibration();
					} else if (e.getActionCommand().equals("DebugSet")) {
						log(String.format("Atempt to AdvanceSet at %s ",
								getTime()));
						AdvanceSet();
					} else if (e.getActionCommand().equals("DebugRun")) {
						log(String.format("%s\r\nAtempt to debug at %s ",
								getTime()));
						(new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Maininstance_.debug();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						})).start();
					}

				} catch (Exception ee) {
					 
				}
			}
		};

		Start.addActionListener(menuListener);
		Live.addActionListener(menuListener);
		Pause.addActionListener(menuListener);
		Save.addActionListener(menuListener);
		Stop.addActionListener(menuListener);
		Set.addActionListener(menuListener);
		Calibration.addActionListener(menuListener);
		DebugRun.addActionListener(menuListener);
		DebugSet.addActionListener(menuListener);

	}

	public void log(String str) {
		record.setText(String.format("%s\r\n     %s ", record.getText(), str));
		record.setCaretPosition(record.getText().length());
	}

	private String getTime() {
		Calendar theCa;
		String nowT;
		theCa = new GregorianCalendar();
		theCa.getTime();
		nowT = theCa.get(Calendar.MONTH) + "/" + theCa.get(Calendar.DATE)
				+ "  "
				+ (24 * theCa.get(Calendar.AM) % 24 + theCa.get(Calendar.HOUR))
				+ ":" + theCa.get(Calendar.MINUTE) + ":"
				+ theCa.get(Calendar.SECOND);
		return nowT;
	}

	long now() {
		Calendar theCa;
		theCa = new GregorianCalendar();
		return theCa.getTimeInMillis();
	}

	public void start() {
		begin = now();
	}

	public void end(String str) {
		long end = now();
		log(String.format("%sCost:%d", str, end - begin));
	}

	private void saveFile() {
		JFileChooser file = new JFileChooser();
		if (file.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File fl = file.getSelectedFile();
			try {
				FileWriter out = new FileWriter(fl);
				out
						.write("#frame,#xPos/pixel,#yPos/pixel,#zPos/pixel,#force/pN\r\n");
				for (int i = 0; i < this.resultSave.length; i++) {
					for (int j = 0; j < this.resultSave[i].length; j++) {
						out.write(this.resultSave[i][j] + ",");
					}
					out.write("\r\n");
				}
				out.close();
			} catch (IOException e) {
				log("File save error");
			}
		}// if Open File
	}
}
