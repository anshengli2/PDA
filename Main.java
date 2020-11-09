import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;

public class Main {
  // We will store each transition rules into this ArrayList
  // with each index in the array representing a state
  private ArrayList<Entry> pda_states;
  int num_states;
  int num_transitions;
  String start_state;
  String [] final_states;
  String input;
  String test_input;

  // Each Entry will store the set of rules from
  // the index number to the state number
  // State 0 has 0->0, 0->1
  // [0]->[0]->[1]
  // [1]->[2]
  // [2]->[2]

  /* Generating a list based on the number of states*/
  public void create_pda_states(int size){
    pda_states = new ArrayList<>();
    for(int i = 0; i < size; i++){
      pda_states.add(null);
    }
  }

  /* Sort all the PDA rules into the list in a linked-list fashion */
  public boolean parse_file(){
    try{
      File pda_desc = new File(input);
      Scanner pda_reader = new Scanner(pda_desc);
      boolean first_line = true;
      while(pda_reader.hasNextLine()){
        String line = pda_reader.nextLine();
        String [] tokens = line.split(" ");
        // First line of pda desc. gives info about pda
        if(first_line){
          // Info is separated by spaces
          // with the final states seperated by commas
          // assuming 0 2 5 4,5 or 0 2 5 4
          num_states = Integer.parseInt(tokens[0]);
          num_transitions = Integer.parseInt(tokens[1]);
          start_state = tokens[2];
          // For more than 1 final states
          if(tokens[3].length() > 1){
            String [] fs_tokens = tokens[3].split(",");
            final_states = Arrays.copyOf(fs_tokens, fs_tokens.length);
          }
          // For 1 final state only
          else{
            final_states = new String[1];
            final_states[0] = tokens[3];
          }
          first_line = false;
          create_pda_states(num_states);
        }
        // Every line after the first gives a set of
        // transition rules
        // q q' sigma pop push
        else{
          Entry head = pda_states.get(Integer.parseInt(tokens[0]));
          // Insert first transition rule for that state
          if(head == null){
            Entry new_entry = new Entry(tokens[1],tokens[2],tokens[3],tokens[4], null);
            pda_states.set(Integer.parseInt(tokens[0]), new_entry);
          }
          // Give a state multiple transitions
          else{
            Entry new_entry = new Entry(tokens[1],tokens[2],tokens[3],tokens[4], null);
            new_entry.next = head;
            pda_states.set(Integer.parseInt(tokens[0]), new_entry);
          }
        }
      }
      pda_reader.close();
      return true;
    }
    catch (FileNotFoundException e){
      System.out.println("Bad file");
      return false;
    }    
}

/* Test the PDA with a set of string inputs */
public boolean parse_test_file(){
  try{
      File test_string = new File(test_input);
      Scanner string_reader = new Scanner(test_string);
      boolean first_line = true;
      while(string_reader.hasNextLine()){
        String line = string_reader.nextLine();
        // The first line gives you the number of string
        // inputs, but we don't need it since
        // we are reading line by line anyways
        if(first_line){
          first_line = false;
        }
        else{
          // Testing if the PDA applies to the string
          if(pda_rules(line)){
            System.out.println("True");
          }
          else{
            System.out.println("False");
          }
        }
      }
      string_reader.close();
      return true;
  }
  catch (FileNotFoundException e){
      System.out.println("Bad file");
      return false;
    }
}

/* Using the data structure we have created, we can test each string */
public boolean pda_rules(String line){
  // We start in the start state
  int current_state = Integer.parseInt(start_state);
  Stack<String> stk = new Stack<>();
  // Holds the entry for a transition on epsilon
  Entry on_epsilon = null;
  // Holds the entry for a transition on a valid sigma 
  Entry state_entry = null;
  boolean accept = false;
  
  String symbol;
  while(!accept){
    if(line.length() != 0)
      symbol = Character.toString(line.charAt(0));
    else
      symbol = "";

    Entry head = pda_states.get(current_state);
    //System.out.print(current_state + "-->");
    while(head != null){
      // The entry contains the current symbol
      if((head.sigma).equals(symbol)){
        state_entry = head;
      }
      // Store an epsilon transition in case no valid symbol
      else if((head.sigma).equals("e")){
        on_epsilon = head;
      }
      head = head.next;
    }

    // Prioritize valid symbol transition
    if(state_entry != null){
      // Push into stack if not epsilon
      if(!(state_entry.push).equals("e")){
        stk.push(state_entry.push);
      }
      // Pop from stack if not epsilon and stack is not empty
      else if(!(state_entry.pop).equals("e") && !stk.isEmpty()){
        // Top of stack does not equal the pop value from state
        if(!(stk.peek()).equals(state_entry.pop)){
          return false;
        }
        else{
          stk.pop();
        }
      }
      // Cannot pop on an empty stack
      else if(!(state_entry.pop).equals("e") && stk.isEmpty()){
        return false;
      }

      // Change to next state
      current_state = Integer.parseInt(state_entry.state);
      if(line.length() > 1){
        line = line.substring(1);
      }
      else{
        line = "";
      }
      
    }
    // Will do an epsilon transition if no valid symbol
    else if(on_epsilon != null){
      if(!(on_epsilon.push).equals("e")){
        stk.push(on_epsilon.push);
      }
      else if(!(on_epsilon.pop).equals("e") && !stk.isEmpty()){
        if(!(stk.peek()).equals(on_epsilon.pop)){
          return false;
        }
        else{
          stk.pop();
        }
      }
      else if(!(on_epsilon.pop).equals("e") && stk.isEmpty()){
        return false;
      }

      current_state = Integer.parseInt(on_epsilon.state);
    }
    // Only two possible case: transition on valid symbol or on epsilon
    else{
      return false;
    }

    // Reset states holder
    state_entry = null;
    on_epsilon = null;

    //System.out.println(current_state);
    //System.out.println(stk.toString());
    // Accept states
    if(line.length() == 0){
      for(int x = 0; x < final_states.length; x++){
        if(current_state == Integer.parseInt(final_states[x])){
          accept = true;
        }
      }
    }
  }
  return true;
}

public void print(){
  for(int i = 0; i < pda_states.size(); i++){
    Entry head = pda_states.get(i);
    while(head != null){
      System.out.println(i + " -> " + head.state + ", Sigma: " + head.sigma + " Pop: " + head.pop + " Push: " + head.push);
      head = head.next;
    }
  }
}

private static class Entry {
  String state;
  String sigma;
  String pop;
  String push;
  Entry next;

  public Entry (String s, String sg, String pp, String psh, Entry n){
    state = s;
    sigma = sg;
    pop = pp;
    push = psh;
    next = n;
  }
}

public static void main(String[] args) {
  while(true){
    Main pda = new Main();
    boolean good_file = false;
    do{
      // Reading the PDA description from an input file
      String pda_input;
      Scanner sc = new Scanner(System.in);
      System.out.print("Enter input file for PDA description: ");
      pda_input = sc.nextLine();
      pda.input = pda_input;
      if(pda.parse_file()){
        good_file = true;
      }
    }while(!good_file);
    
    pda.print();

    good_file = false;
    do{
      // Reading the test strings from an input file
      String string_input;
      Scanner sc2 = new Scanner(System.in);
      System.out.print("Enter test file for PDA: ");
      string_input = sc2.nextLine();
      pda.test_input = string_input;
      if(pda.parse_test_file()){
        good_file = true;
      }
    }while(!good_file);
    System.out.println();
    }
  }
}