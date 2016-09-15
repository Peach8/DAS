// Particle.java
// -- 
// Author: Brandon Peterson

import java.util.Random;

public class Particle {
	// variables
	private int[] pos;
	private int range;
	private Random rand;

	// constructors
	public Particle(Random r, int rng) {
		this.range = rng;
		this.rand = r;
		this.pos = new int[] {rand.nextInt(this.range), rand.nextInt(this.range)};
	}

	// methods
	public int[] getPos() {
		return this.pos;
	}

	public void step(String topology) {
		// randomly step to an adjacent pixel in Moore neighborhood
		int step = rand.nextInt(8);
		switch(step) {
			case 0: this.pos[1]++; // move down 1
					break;
			case 1: this.pos[0]--; // move left 1
					break;
			case 2: this.pos[1]--; // move up 1
					break;
			case 3: this.pos[0]++; // move right 1
					break;
			case 4: this.pos[0]--; // move left 1
					this.pos[1]++; // & down 1
					break;
			case 5: this.pos[0]--; // move left 1
					this.pos[1]--; // & up 1
					break;
			case 6: this.pos[0]++; // move right 1
					this.pos[1]--; // & up 1
					break;
			case 7: this.pos[0]++; // move right 1
					this.pos[1]++; // & down 1
					break;		
		}
		switch (topology) {
			case "bounded": 
			// bounded plane -
			// don't allow movement through boundaries,
			// i.e., "bounce off walls"
				if (this.pos[0] == this.range) {
					this.pos[0] = this.range-1;
				}
				else if (this.pos[0] < 0) {
					this.pos[0] = 0;
				}
				if (this.pos[1] == this.range) {
					this.pos[1] = this.range-1;
				}
				else if (this.pos[1] < 0) {
					this.pos[1] = 0;
				}							
				break;
			case "toroid": 
			// toroidal plane -
			// connect top/bottom boundaries and left/right 
			// note: opposite corners are connected as a result
				if (this.pos[0] == this.range) {
					this.pos[0] = 0;
				}
				else if (this.pos[0] < 0) {
					this.pos[0] = this.range-1;
				}
				if (this.pos[1] == this.range) {
					this.pos[1] = 0;
				}
				else if (this.pos[1] < 0) {
					this.pos[1] = this.range-1;
				}						
				break;			
		}
	}
}