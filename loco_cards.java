import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.lang.RuntimeException;

/*
Test case 1:
4
R2 Y8 G4 G6 G7 
R7 Y3 Y6 B2 B4
R6 R8 Y1 B1 B5
R1 R3 R5 Y7 B6
B7
G1 G3 Y2 G2 B8 Y4
*/

public class Problem4 {
	
	static int N;

	public static String[] parseLine(String line) {
		return line.trim().split(" ");
	}

	public static void main(String[] args) { 
    	Scanner sc = new Scanner(System.in);
    	N = Integer.valueOf(sc.nextLine());
    	String[][] player_cards = new String[N][5];
    	String[] deck;
    	String discard;

    	for (int i = 0; i < N; i++) {
    		player_cards[i] = parseLine(sc.nextLine());
    	}

    	discard = sc.nextLine();
    	deck = parseLine(sc.nextLine());

    	Game game = new Game(N, player_cards, discard, deck);

    	game.play();
	}
}

class Card {
	public int color;
	public int num;

	/* 
		For color:
		R:0, Y:1, G:2, B:3 
	*/
	public static String[] dict = {"R", "Y", "G", "B"};

	Card(int color, int num) {
		this.color = color;
		this.num = num;
	}

	public String toString() {
		return dict[this.color] + String.valueOf(num);
	}

}

class Game {
	int N;
	ArrayList<ArrayList<Card>> players = new ArrayList<ArrayList<Card>>();
	Card discarded;
	Queue<Card> deck = new LinkedList<Card>();
	boolean take_two_finish;

	/* Game initialize */
	Game(int N, String[][] player_cards, String discarded, String[] deck) {
		ArrayList<Card> player;
		this.N = N;
		
		for (int i = 0; i < N; i++) {
			player = new ArrayList<Card>();
			for (int j = 0; j < 5; j++) {
				player.add(this.parseCard(player_cards[i][j]));
			}
			players.add(player);
		}

		this.discarded = this.parseCard(discarded);

		for (int k = 0; k < deck.length; k++) {
			this.deck.offer(this.parseCard(deck[k]));
		}

		take_two_finish = false;
	}

	private int parseColor(String card) {
		switch (card.substring(0, 1)) {
			case "R" :
				return 0;
			case "Y" :
				return 1;
			case "G" :
				return 2;
			case "B" :
				return 3;
			default:
				throw new RuntimeException("Unknown card color, please check the input");
		}
	}

	private int parseNumber(String card) {
		return Integer.valueOf(card.substring(1, 2));
	}


	private Card parseCard(String card) {
		return new Card(this.parseColor(card), this.parseNumber(card));
	}

	private void postMove(ArrayList<Card> cards, Card res) {
		this.discarded = res;
		cards.remove(cards.indexOf(res));
	}

	private Card move(ArrayList<Card> cards) {
		Card color_choice = null, num_choice = null, res = null;
		int color_number = 0, num_number = 0;
		/* take two branch */
		if (this.discarded.num == 2 && !this.take_two_finish) {
			for (Card c : cards) {
				if (c.num == 2) {
					if (res == null || (res != null && c.color < res.color)) {
						res = c;
					}
				}
			}
			if (res != null)
				postMove(cards, res);
			return res;
		} else {
			for (Card c : cards) {
				if (c.color == this.discarded.color) {
					if (color_choice == null || (color_choice != null && c.num < color_choice.num)) {
						color_choice = c;
					}
					color_number++;
				}
				if (c.num == this.discarded.num) {
					if (num_choice == null || (num_choice != null && c.color < num_choice.color)) {
						num_choice = c;
					}
					num_number++;
				}
				/* LOCO rule */
				if (this.discarded.num == 8 && c.num == 8) {
					postMove(cards, c);
					return c;
				}

				if (c.num == 8 && this.discarded.color == c.color) {
					postMove(cards, c);
					return c;
				}
			}
		}

		if (color_number == 0 && num_number == 0) {
			return null;
		}

		res =  num_number > color_number ? num_choice : color_choice;
		/* Update cards in hand and the discarded card*/
		postMove(cards, res);
		return res;
	}

	private void draw(ArrayList<Card> cards, int num) {
		Card draw_card;
		int cards_num = cards.size();

		for (int i = 0; i < num; i++) {
			draw_card = this.deck.poll();
			/* Make cards in order */
			for (int j = 0; j < cards_num; j++) {
				if (draw_card.color < cards.get(j).color) {
					cards.add(j, draw_card);
					break;
				}
				/* Add in tail */
				if (j == cards_num - 1) {
					cards.add(draw_card);
				}
			}
		}
	}

	private boolean playerMove(int player) {
		String msg;
		ArrayList<Card> cards_in_hand;
		Card res;

		// System.out.println("discarded : " + this.discarded.toString());
		msg = String.valueOf(player + 1) + ": ";
		cards_in_hand = this.players.get(player);

		res = move(cards_in_hand);

		if (res != null) {
			msg += res.toString();
			this.take_two_finish = false;

			if (cards_in_hand.size() == 0) {
				msg += " (WINNER)";
				System.out.println(msg);
				return true;
			}

			if (this.discarded.num == 8) {
				/* Change the target color */
				this.discarded.color = cards_in_hand.get(0).color;
				msg += " LOCO " + Card.dict[this.discarded.color];
			}

		} else if (this.discarded.num == 2 && !this.take_two_finish){
			msg += "TAKE TWO";
			draw(cards_in_hand, 2);
			this.take_two_finish = true;
		} else {
			msg += "DRAW";
			draw(cards_in_hand, 1);
		}

		System.out.println(msg);
		// System.out.println(cards_in_hand.toString());
		return false;
	}

	public void play() {
		int round = 0;
		boolean end = false;

		System.out.printf("%d: ", round);
		System.out.println(discarded.toString());
		
		while (!end) {
			if (playerMove(round % this.N)) 
				end = true;
			round++;
		}
	}
}
