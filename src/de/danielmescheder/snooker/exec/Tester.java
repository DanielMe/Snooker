package de.danielmescheder.snooker.exec;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.Timer;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.simulation.InTimeSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.SetValuesEvent;
import de.danielmescheder.snooker.simulation.physics.Physics;

public class Tester extends JFrame
{
	private static final long serialVersionUID = -6411014878802068316L;
	public Timer timer;
	public boolean paused;
	public static boolean detailMode;
	private BilliardBall[] balls;
	private int currentBall = 0;
	private Simulation sim;

	// This 2D array saves the vectors 'center' , 'velRef' and 'spinRef'
	// ballVectors [ball][center]
	// [ball][velRef]
	// [ball][spinRef]

	private Vector3f[][] ballVectors;

	private float mpp; // meters per pixel;

	private Color backgroundColor, foregroundColor, lineColor;

	private static final int RADIUS = 15;
	private boolean depthAdj = false, simRunning = false;

	private static Tester sFrame;
	private static float sMPP;

	public Tester()
	{

		detailMode = false;

		foregroundColor = new Color(200, 100, 0);
		backgroundColor = new Color(50, 100, 50);
		lineColor = new Color(50, 100, 200);

		mpp = 0.0254f / this.getToolkit().getScreenResolution();
		mpp = 5 * mpp;
		setBackground(backgroundColor);


		sFrame = this;
		sMPP = mpp;

		//
		// Change this to change the number of balls on the table
		//
		balls = new BilliardBall[3];

		ballVectors = new Vector3f[balls.length][3];

		class TimerListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				// clearBall();
				sim.advanceTime(0.01f);
				for (BilliardBall ball : balls)
				{
					sim.getBall(ball);
				}

				drawBall();

			}

		}
		timer = new Timer(10, new TimerListener());

		class MEvents extends MouseAdapter
		{

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (currentBall < balls.length
						&& ballVectors[currentBall][0] != null
						&& ballVectors[currentBall][1] != null
						&& ballVectors[currentBall][2] == null)
				{
					depthAdj = true;
					ballVectors[currentBall][2] = new Vector3f(e.getX(), e
							.getY(), 0);

				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (depthAdj)
				{
					depthAdj = false;
					ballVectors[currentBall][2].z = -(e.getY() - ballVectors[currentBall][2].y);

				}
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (currentBall < balls.length)
				{
					if (ballVectors[currentBall][0] == null
							&& ballVectors[currentBall][1] == null)
					{
						ballVectors[currentBall][0] = new Vector3f(e.getX(), e
								.getY(), 0);
					}
					else if (ballVectors[currentBall][0] != null
							&& ballVectors[currentBall][1] == null)
					{
						ballVectors[currentBall][1] = new Vector3f(e.getX(), e
								.getY(), 0);
					}
					else if (ballVectors[currentBall][2] != null)
					{
						currentBall++;
						drawCircles();

					}

				}
				else if (currentBall == balls.length)
				{

					if (!simRunning)
					{
						initSim();
						simRunning = true;
					}
					else
					{
						if (paused)
						{
							Graphics2D g2 = (Graphics2D) sFrame.getGraphics();
							g2.clearRect(0, 0, sFrame.getWidth(), sFrame
									.getHeight());
							for (BilliardBall ball : balls)
							{
								drawBall(ball, foregroundColor);
							}
							timer.start();
							paused = false;
						}
						else
						{
							currentBall = 0;
							simRunning = false;
							ballVectors = new Vector3f[balls.length][3];
							drawSketch(0, 0);
							timer.stop();
						}
					}
				}
			}
		}
		MEvents e = new MEvents();
		this.addMouseListener(e);
		this.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (depthAdj)
				{
					drawSketch(e.getX(), e.getY());
				}
			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				if (currentBall < balls.length
						&& ballVectors[currentBall][0] != null)
				{
					drawSketch(e.getX(), e.getY());
				}

			}
		});
	}

	public void initSim()
	{
		System.out.println("=======================");
		System.out.println("Initializing Simulation");
		System.out.println("=======================");
		Set<BilliardBall> set = new HashSet<BilliardBall>();
		Set<Event> initialEvents = new HashSet<Event>();
		for (int i = 0; i < balls.length; i++)
		{
			balls[i] = new BilliardBall(i);
			balls[i].setRadius(RADIUS * mpp);
			balls[i].setPosition(ballVectors[i][0].mult(mpp));
			balls[i].getPosition().y = this.getHeight() * mpp
					- ballVectors[i][0].mult(mpp).y;
			balls[i].setMass(160);

			Vector3f v = ballVectors[i][1].subtract(ballVectors[i][0])
					.mult(mpp);
			v.y = -v.y;
			Vector3f av = new Vector3f(ballVectors[i][2].x
					- ballVectors[i][0].x, ballVectors[i][0].y
					- ballVectors[i][2].y, ballVectors[i][2].z).mult(mpp * 100);
			SetValuesEvent e = new SetValuesEvent(0, balls[i], v, av,
					BallState.SLIDING);

			initialEvents.add(e);
			set.add(balls[i]);
		}
		Graphics2D g2 = (Graphics2D) this.getGraphics();
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
		for (BilliardBall ball : balls)
		{
			drawBall(ball, foregroundColor);
		}
		drawLine(0.1f, 0f, 0.1f, 1.0f, Color.BLACK);
		drawLine(1.0f, 0f, 1.0f, 1.0f, Color.BLACK);
		drawLine(0f, 0.1f, 1f, 0.1f, Color.BLACK);
		drawLine(0f, 1f, 1f, 1f, Color.BLACK);
		timer.start();

		System.out.println(set);
		System.out.println(initialEvents);
		sim = new InTimeSimulation(set, new Table(0, 0, 0, 0, 0, 0, 0, 0));
		sim.init(initialEvents);
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
	}

	public void drawSketch(int x, int y)
	{
		Graphics2D g2 = (Graphics2D) this.getGraphics();
		g2.setColor(lineColor);
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
		Vector3f center = ballVectors[currentBall][0];
		Vector3f velRef = ballVectors[currentBall][1];
		Vector3f spinRef = ballVectors[currentBall][2];

		if (center == null && velRef == null)
		{

		}
		else if (center != null && velRef == null)
		{
			int cx = (int) (center.x - RADIUS);
			int cy = (int) (center.y - RADIUS);

			g2.drawOval(cx, cy, 2 * RADIUS, 2 * RADIUS);
			g2.drawLine((int) center.x, (int) center.y, x, y);
		}
		else if (spinRef == null)
		{
			int cx = (int) (center.x - RADIUS);
			int cy = (int) (center.y - RADIUS);

			g2.drawOval(cx, cy, 2 * RADIUS, 2 * RADIUS);
			g2.drawLine((int) center.x, (int) center.y, (int) velRef.x,
					(int) velRef.y);
			g2.drawLine((int) center.x, (int) center.y, x, y);
		}
		else if (depthAdj)
		{
			int cx = (int) (center.x - RADIUS);
			int cy = (int) (center.y - RADIUS);

			g2.drawOval(cx, cy, 2 * RADIUS, 2 * RADIUS);
			g2.drawLine((int) center.x, (int) center.y, (int) velRef.x,
					(int) velRef.y);
			g2.drawLine((int) center.x, (int) center.y, (int) spinRef.x, y);
			g2.drawLine((int) center.x, (int) center.y, (int) spinRef.x,
					(int) spinRef.y);
			g2.drawLine((int) spinRef.x, (int) spinRef.y, (int) spinRef.x, y);

		}
		drawCircles();
	}

	public void drawCircles()
	{
		for (int i = 0; i < currentBall; i++)
		{
			Vector3f center = ballVectors[i][0];
			Vector3f velRef = ballVectors[i][1];
			int x = (int) (center.x - RADIUS);
			int y = (int) (center.y - RADIUS);
			Graphics2D g2 = (Graphics2D) this.getGraphics();
			g2.setColor(foregroundColor);
			g2.fillOval(x, y, 2 * RADIUS, 2 * RADIUS);
			g2.drawLine((int) center.x, (int) center.y, (int) velRef.x,
					(int) velRef.y);
		}

	}

	boolean brighter = true;

	public void drawBall()
	{
		Graphics2D g2 = (Graphics2D) this.getGraphics();
		Color currentColor = foregroundColor;
		for (BilliardBall ball : balls)
		{
			if (ball != null)
			{

				if (ball.getState() == BallState.SLIDING)
				{
					currentColor = Color.RED;
				}
				else if (ball.getState() == BallState.ROLLING)
				{
					currentColor = Color.YELLOW;
				}
				else
				{
					currentColor = Color.BLUE;
				}
				if (ball.getState() != BallState.RESTING)
				{
					if (brighter)
					{
						currentColor = currentColor.darker();
					}
					else
					{
						currentColor = currentColor.brighter();
					}
				}
				g2.setColor(currentColor);
				g2
						.fillOval((int) (ball.getPosition().x / mpp - ball
								.getRadius()
								/ mpp), (int) (this.getHeight()
								- ball.getPosition().y / mpp - ball.getRadius()
								/ mpp), (int) (2 * ball.getRadius() / mpp),
								(int) (2 * ball.getRadius() / mpp));
				// System.out.println("Drawing Ball: " + ball.toString());
			}
		}
		brighter = !brighter;

	}

	public static void drawBall(BilliardBall b0, Color c)
	{
		Graphics2D g2 = (Graphics2D) sFrame.getGraphics();
		g2.setColor(c);
		g2.fillOval((int) (b0.getPosition().x / sMPP - b0.getRadius() / sMPP),
				(int) (sFrame.getHeight() - b0.getPosition().y / sMPP - b0
						.getRadius()
						/ sMPP), (int) (2 * b0.getRadius() / sMPP),
				(int) (2 * b0.getRadius() / sMPP));
	}

	public static void drawLine(float xFrom, float yFrom, float xTo, float yTo,
			Color c)
	{
		Graphics2D g2 = (Graphics2D) sFrame.getGraphics();
		g2.setColor(c);
		g2.drawLine((int) (xFrom / sMPP), (int) (sFrame.getHeight() - yFrom
				/ sMPP), (int) (xTo / sMPP), (int) (sFrame.getHeight() - yTo
				/ sMPP));

	}

	public static void drawV(BilliardBall b0, Color c, float scale)
	{
		Tester.drawLine(b0.getPosition().x, b0.getPosition().y, b0
				.getPosition().x
				+ b0.getVelocity().mult(scale).x, b0.getPosition().y
				+ b0.getVelocity().mult(scale).y, c);
	}

	public static void drawAV(BilliardBall b0, Color c, float scale)
	{
		Tester.drawLine(b0.getPosition().x, b0.getPosition().y, b0
				.getPosition().x
				+ b0.getAngularVelocity().mult(scale).x, b0.getPosition().y
				+ b0.getAngularVelocity().mult(scale).y, c);
	}

	public static void drawU(BilliardBall b0, Color c, float scale)
	{
		Vector3f u = Physics.getRelativeVelocity(b0).mult(scale);
		Tester.drawLine(b0.getPosition().x, b0.getPosition().y, b0
				.getPosition().x
				+ u.x, b0.getPosition().y + u.y, c);
	}

	public static void markPoint(float x, float y, String desc, Color c)
	{
		Graphics2D g2 = (Graphics2D) sFrame.getGraphics();
		g2.setColor(c);
		g2.drawLine((int) (x / sMPP), 0, (int) (x / sMPP), sFrame.getHeight());
		g2.drawLine(0, (int) (sFrame.getHeight() - y / sMPP),
				sFrame.getWidth(), (int) (sFrame.getHeight() - y / sMPP));
		g2.drawString(desc, 10, (int) (sFrame.getHeight() - y / sMPP) - 7);

	}

	public static void pause()
	{
		sFrame.timer.stop();
		sFrame.paused = true;
	}

	public static void main(String[] args)
	{
		Tester t = new Tester();
		t.setVisible(true);
		t.setSize(new Dimension(1280, 800));
		t.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
