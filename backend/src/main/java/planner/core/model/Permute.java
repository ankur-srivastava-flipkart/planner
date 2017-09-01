package planner.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kumar.vivek on 23/06/17.
 */
public class Permute {
  /* arr[]  ---> Input Array
    data[] ---> Temporary array to store current combination
    start & end ---> Staring and Ending indexes in arr[]
    index  ---> Current index in data[]
    r ---> Size of a combination to be printed */
  private List<List<Person>> combinations = new ArrayList<>();

  private void combinationUtil(Person arr[], Person data[], int start,
                               int end, int index, int r)
  {
    // Current combination is ready to be printed, print it
    if (index == r)
    {
      List<Person> people = new ArrayList<>();
      for (int j=0; j<r; j++) {
      //  System.out.print(data[j] + " ");
        people.add(data[j]);
      }
      combinations.add(people);
     // System.out.println("");
      return;
    }

    // replace index with all possible elements. The condition
    // "end-i+1 >= r-index" makes sure that including one element
    // at index will make a combination with remaining elements
    // at remaining positions
    for (int i=start; i<=end && end-i+1 >= r-index; i++)
    {
      data[index] = arr[i];
      combinationUtil(arr, data, i + 1, end, index + 1, r);
    }
  }

  // The main function that prints all combinations of size r
  // in arr[] of size n. This function mainly uses combinationUtil()
  private List<List<Person>> getCombination(Person arr[], int arrLength, int batchSize)
  {
    combinations = new ArrayList<>();

    // A temporary array to store all combination one by one
    Person data[]=new Person[batchSize];

    // return all combination using temprary array 'data[]'
    combinationUtil(arr, data, 0, arrLength-1, 0, batchSize);

    return combinations;
  }

  public static List<List<Person>> getAllCombinations(Person arr[], int arrLength, int batchSize) {
    Permute permute = new Permute();
    return permute.getCombination(arr, arrLength, batchSize);
  }
}
