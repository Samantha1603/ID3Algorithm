package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ID3 
{
	public static DecimalFormat df = new DecimalFormat("##.###");
	public static HashMap<String,Integer> attListMap=new HashMap();//map attribute name with its colno colNo
	public static HashMap<String,String[]> attListMapData=new HashMap();//map attribute name wit its data
	public static ArrayList<String[]> attrList=new ArrayList<>();//store list of attributeData
	public static ArrayList<String[]> mainDataList=new ArrayList<>();//main data
	public static ArrayList<String[]> trainList=new ArrayList<>();//training data list
	public static ArrayList<String[]> testList=new ArrayList<>();//test data
	public static String removeFromFinalTreeData="";//variable that stores the parsed node from the list which then needs to be updated in the final tree data
	public static ArrayList<String> treeData=new ArrayList<>(); //stores the different combinations of the tree structure
	public static ArrayList<String> completedTree=new ArrayList<>(); //stores the final and completed tree structure
	public static int trainCounter=0;// count the number of hits in the training list to calculate accuracy
	public static int testCounter=0;//count number of hits in the testing list to calculate accuracy


	public static void main(String args[])
	{
		int cont=1;
		while(cont==1)
		{
			System.out.println("Decision tree construction using ID3 Algorithm");
			System.out.println("Select dataset:");
			System.out.println("1. Monk data");
			System.out.println("2. Tic Tac Toe dataset");
			
			
			Scanner sc=new Scanner(System.in);
			int option=sc.nextInt();
			String attrFile="";
			String mainFile="";
			switch(option)
			{
			
			case 1: attrFile="ML second version/../files/Attribute4.csv";//attribute file location 
			mainFile="ML second version/../files/Monk_data - Copy.csv";//main file location
			break;
			
			case 2: attrFile="ML second version/../files/Attribute1.csv";//attribute file location 
			mainFile="ML second version/../files/TTT.csv";//main file location
			break;
			
		

			default:System.out.println("Please choose the right option");
			break;
			}

			trainList.clear();
			testList.clear();
			mainDataList.clear();
			attrList.clear();
			trainCounter=0;
			testCounter=0;

			int trainPercent=80;
			int testPercent=20;
			//read attibute file
			attrList=readFile(attrFile);//read attribute file

			for(int i=0;i<attrList.size()-1;i++)
			{
				attListMap.put(attrList.get(i)[0],i);
				String[] a=new String[attrList.get(i).length-2];
				for(int j=2;j<attrList.get(i).length;j++)
				{
					a[j-2]=attrList.get(i)[j];
				}
				attListMapData.put(attrList.get(i)[0], a);
			}

			System.out.println();
			//read training data file
			mainDataList=readFile(mainFile);

			//create data for train
			trainList=createTrainAndTestData(mainDataList,trainPercent);
			//printList(trainList, trainList.size());

			//create data for test		
			testList=createTrainAndTestData(mainDataList, testPercent);

			System.out.println("Number of instances in the main data set are "+mainDataList.size());
			System.out.println("Number of instances randomly selected to train the decision tree are "+trainList.size());

			//make decision tree 
			calculateTree(trainList);

			//print out the completed tree with the target value
			computeCompleteTree(trainList);

			System.out.println();
			System.out.println("Number of instances randomly selected to test the decision tree are "+testList.size());
			System.out.println("Press y to calculate accuracy");
			String calAccuracy=sc.next();
			if(calAccuracy.equalsIgnoreCase("y"))
			{
				System.out.println("Accuracy of the Train data is:");

				calculateTrainAccuracy(trainList);//calculate training set accuracy
				System.out.println();
				System.out.println("Accuracy of the Test data is:");
				calculateTestAccuracy(testList);//calculate accuracy based on test data
			}
			System.out.println("Do you want to continue? 1=Yes or 0=No");
			cont=sc.nextInt();
			clearGlaobalLists();
			trainList.clear();
			attrFile="";
			mainFile="";
		}

	}

	//function to create training and testing data set. percent denotes the amount of data in training or testing from the maindata file
	public static ArrayList<String[]> createTrainAndTestData(ArrayList<String[]> mainData,int percent)
	{
		ArrayList<String[]> createData=new ArrayList<>();
		createData.clear();
		int size=mainData.size()*percent/100;//calculating the exact size of the data set based on the percent specified.
		for(int i=0;i<size;i++)
		{
			createData.add(mainData.get(calculateRandom(mainData.size(),i)));//randomly pick up a row from the mainData file 
			// and add it to the data-set 
		}
		return createData;
	}

	//generate a random number of rows
	private static int calculateRandom(int size, int i) {
		Random rand = new Random();
		int	x = rand.nextInt(size) + 0;
		return x;
	}

	//main driver function to calculate decision tree
	public static void calculateTree(ArrayList<String[]> trainList)
	{	
		if(treeData.isEmpty())//Root node not yet found
		{
			double entropy=calculateEntropy(trainList);
			if(entropy==-0.0)//data contains only pure set . eg either all yes or all no
			{
				//do nothing
			}
			else
			{
				String highestInfoGainNode=calculateInformationGain(entropy,trainList,"");//calculate node that has highest info gain
				if(highestInfoGainNode.equals(""))
				{
					//no node found....do nothing
				}
				else
				{
					String[] highValues=attListMapData.get(highestInfoGainNode);//calculate values of the highest-info gain value node i.e a,b,c for A
					treeData.remove(removeFromFinalTreeData);//remove the node from tree data 
					for(int i=0;i<highValues.length;i++)//appending the removed value along with the highest nodes and its values to the final tree data
					{
						//if A-a is one of the tree node data and the highest info gain node for it is B then remove A-a from
						//treeData and add A-a:B-a and A-a:B-b as elements in arrayLists if attributes values of B are a,b
						if(!removeFromFinalTreeData.equals(""))
							treeData.add(removeFromFinalTreeData+":"+highestInfoGainNode+"-"+highValues[i]);
						else
							treeData.add(highestInfoGainNode+"-"+highValues[i]);

					}
				}
				calculateTree(trainList);//calculate remaining tree...loop till entropy is not 0  i.e pure set not reached yet
			}
		}
		else
		{
			//root node has been found now to calculate the remaining nodes
			ArrayList<String> tempList=new ArrayList<>();
			tempList.addAll(treeData);//finaltreedata stores so far constructed tree structure in following form. 
			//if A,B,C are nodes and if A is the root with values a,b,c the after the first iteration
			//finaltreedata will store A-a,A-b,A-c indicating all possible leaf nodes of A.
			for(int j=0;j<tempList.size();j++)
			{
				removeFromFinalTreeData=tempList.get(j);//removeFromFinalTreeData will store ith value of templist eg:A-a
				ArrayList<String[]> newList=new ArrayList<>();
				newList=extractNewList(tempList.get(j),trainList);//extract newlist from trainlist based on ith tempList value
				//for A-a, extract from trainList the data 
				//for whom the Ath attribute has value a

				double entropy=calculateEntropy(newList);
				if(entropy==-0.0)//data contains only one target value . eg either all yes or all no
				{
					//do nothing
					completedTree.add(tempList.get(j));
					treeData.remove(tempList.get(j));
				}
				else
				{
					String highestInfoGainNode=calculateInformationGain(entropy,newList,tempList.get(j));//calculate node that has highest info gain
					if(highestInfoGainNode.equals(""))
					{
						//no node found do nothing
					}
					else
					{
						//attrList.remove(aList.get(highestInfoGainNode));//remove the node from the attrlist as we will add the node to the finaltreedata structure
						//so no need to compute again
						String[] highValues=attListMapData.get(highestInfoGainNode);//calculate values of the highest value node i.e a,b,c for A
						treeData.remove(removeFromFinalTreeData);//remove the node from final tree data 
						for(int i=0;i<highValues.length;i++)//appending the removed value along with the highest nodes and its values to the final tree data
						{
							if(!removeFromFinalTreeData.equals(""))
								treeData.add(removeFromFinalTreeData+":"+highestInfoGainNode+"-"+highValues[i]);
							else
								treeData.add(highestInfoGainNode+"-"+highValues[i]);

						}
						calculateTree(trainList);//calculate tree for remaining attributes
					}

				}
			}
		}


	}

	//function to compute complete tree based on values in treedata and get the target value and add it to the list by appending it to 
	// the respective value
	private static void computeCompleteTree(ArrayList<String[]> trainList) 
	{

		ArrayList<String[]> newList=new ArrayList<>();
		int size=completedTree.size();
		for(int i=0;i<size;i++)//looping over size of the finaltreedata
		{
			//System.out.println(completedTree.get(i));
			newList=extractNewList(completedTree.get(i),trainList);//extracting new list to get the final target value (yes no)
			//System.out.println("for i="+i+" newList size="+newList.size());
			if(newList.size()>0)
			{	
				String val=calculateFinalAns(newList);
				String add=completedTree.get(i)+"-->"+val;//appending the target value to the tree data by --> seperator eg:  A-a:B-g-->yes
				completedTree.remove(i);//if ith value is A-a:B-g remove it from final tree data
				completedTree.add(i, add);// add to finaltreedata the value of 'add' at ith location
			}
			{
				if(completedTree.get(i).contains("-->"))
				{
					//do nothing
				}
				else
				{
					String add=completedTree.get(i)+"-->NA";//appending the target value to the tree data by --> seperator eg:  A-a:B-g-->yes
					completedTree.remove(i);//if ith value is A-a:B-g remove it from final tree data
					completedTree.add(i, add);// add to finaltreedata the value of 'add' at ith location
				}
			}
		}
	}


	//function to extract list based on the value passed in treedatavalue
	private static ArrayList<String[]> extractNewList(String treeDataValue,ArrayList<String[]> trainList) 
	{
		String[] splitString=treeDataValue.split(":");//treedatavalue contains the value of the already parsed node and its values
		// structure is A-a,A-b,A-c oor A-a:B-h,A-b:C-d:e:l etc
		//split on : to get the number of nodes 
		ArrayList<String[]> extractedList=new ArrayList<>();
		extractedList.clear();
		extractedList.addAll(trainList);
		int size=trainList.size();
		for(int i=0;i<size;i++)
		{
			int flag=0;
			for(int k=0;k<splitString.length;k++)
			{
				String[] attribute=splitString[k].split("-");//split on - to get the values
				int colNo=attListMap.get(attribute[0]);//get the no of col from which the value of attribute needs to be checked in trained list
				if(!trainList.get(i)[colNo].equalsIgnoreCase(attribute[1]))//if attribute value in specified col does not match the attribute value in tree date
				{
					//extractedList.add(trainList.get(i));
					flag++;//increment flag so that we can remove this data from the extractedlist
				}

			}
			if(flag!=0)//if flag is not 0 that means the data did not match one of the condition
				extractedList.remove(trainList.get(i));	//remove the value from the list
		}
		return extractedList;

	}

	//function to calculate info gain
	private static String calculateInformationGain(double entropy, ArrayList<String[]> trainList,String finalTreeDataSent) 
	{
		ArrayList<String> attributes=new ArrayList<>();

		for(int i=0;i<attrList.size()-1;i++)
		{
			attributes.add(attrList.get(i)[0]);
		}

		if(finalTreeDataSent.length()!=0)
		{
			String[] a=finalTreeDataSent.split(":");
			for(int j=0;j<a.length;j++)
			{
				if(attributes.contains(a[j].split("-")[0]))
					attributes.remove(a[j].split("-")[0]);
			}
		}
		HashMap<String, Double> infoGainList=new HashMap<>();//contains inof gain for all remaining attributes(features)
		for(int i=0;i<attributes.size();i++)
		{
			double infoGainForEachAttr=extractInfoGainForEachAttribute(trainList,attributes.get(i));//info gain for each attribute(summation part of the formula)
			double totalInfoGain=Double.parseDouble(df.format(entropy-infoGainForEachAttr));//subtract it from the entropy to get the total info gain
			infoGainList.put(attributes.get(i), totalInfoGain);//put it in a hashmap
		}
		String highestInfoGainNode="";
		double tem=0;
		//calculate highest value of info gain
		for (Map.Entry<String,Double> entry : infoGainList.entrySet()) //loop to calculate highest info gain
		{
			if(entry.getValue()>tem)
			{
				tem=entry.getValue();
				highestInfoGainNode=entry.getKey();
			}
		}
		return highestInfoGainNode;//return value of the node with highest info gain
	}

	//function to calculate info gain for each attribute
	private static double extractInfoGainForEachAttribute(ArrayList<String[]> trainList,
			String attrListN) 
	{
		int noOfValues=0;
		double entropyNForattr=0;
		double totalInforGain=0;
		String[] attrListNames2=attListMapData.get(attrListN);
		for(int j=0;j<attrListNames2.length;j++)
		{
			ArrayList<String[]> extractedList=new ArrayList<>();
			for(int i=0;i<trainList.size();i++)//extrcat list based on the attribute value
			{
				if(trainList.get(i)[attListMap.get(attrListN)].equalsIgnoreCase(attrListNames2[j]))
				{
					extractedList.add(trainList.get(i));
				}
			}	
			entropyNForattr=calculateEntropy(extractedList);//calculate entropy for that list
			totalInforGain=totalInforGain+inforGainForEachAttribute(trainList.size(),extractedList.size(),entropyNForattr);//total info gain for that list(based on specific value)
			//add it to the already calculated info gain
			extractedList.clear();
		}
		return totalInforGain;//return total info gain for that attribute
	}

	//function to read data from a file
	static ArrayList<String[]> readFile(String fileName)
	{
		try {

			ArrayList<String[]> valueList=new ArrayList<>();
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] values = line.split(cvsSplitBy);
				valueList.add(values);
			}
			return valueList;


		} catch (Exception e)//file not found exception
		{
			e.printStackTrace();
			return null;
		} 	

	}

	//function to print list of data in arraylist of string array
	static void printList(ArrayList<String[]> valueList,int n)
	{
		for(int j=0;j<n;j++)
		{
			for(int i=0;i<valueList.get(j).length;i++)
			{
				System.out.print(valueList.get(j)[i]+",");
			}
			System.out.println();
		}
	}

	//function to calculate the target value in the final tree data structure
	static String calculateFinalAns(ArrayList<String[]> trainList)
	{
		String[] trainData=trainList.get(0);//get the 0th target value as all the other rows in the array will have the same target value
		//System.out.println();
		int n=trainData.length-1;
		String val=trainData[n];
		return val;
	}

	//function to calculate entropy
	static double calculateEntropy(ArrayList<String[]> trainList)
	{
		HashMap<String, Integer> map=new HashMap<>();//map to put total count of the distinct target values(key) as values to each key
		for(int i=0;i<trainList.size();i++)
		{
			String[] trainData=trainList.get(i);
			int n=trainData.length-1;
			if(map.get(trainData[n])==null)
				map.put(trainData[n], 1);
			else
				map.put(trainData[n],	map.get(trainData[n])+1);	
		}
		double entropy=0;
		for (Map.Entry<String,Integer> entry : map.entrySet()) //map now stores count of each distinct target value from the parsed list
		{
			int value = entry.getValue();
			double add=((double)value)/((double)trainList.size());
			entropy=entropy+add*log2(add);//calulate entropy. log2 is the fucntion to calculate log to the based two of the value
		}
		entropy=Double.parseDouble(df.format(entropy));
		map.clear();
		//System.out.println(-entropy);
		return -(entropy);
	}


	//function to do calculations for info gain
	public static double inforGainForEachAttribute(int total,int extractedListSize, double attrEntropy)
	{
		double infoGain=0;
		infoGain=((double)((double)extractedListSize/(double)total)*(double)attrEntropy);	//info gain=listsize/total gtrainsize*the entropy of the list
		return infoGain;
	}

	//function to do log calculations
	public static double log2(double num)
	{
		if(num==0)
			return 0;
		else 
			return (Math.log(num)/Math.log(2));
	}

	//calculate accuracy of the test data
	public static void calculateTestAccuracy(ArrayList<String[]> testList)
	{
		int total=testList.size();
		for(int i=0;i<completedTree.size();i++)
		{
			String split[]=completedTree.get(i).split("-->");//split based on --> so at index 1 the split function will store the target value
			calculateTestCount(testList,split);

		}

		double accuracy=((double)testCounter/(double)total)*100;//accuracy =total hits/the no of data*100
		System.out.println("count="+testCounter);
		System.out.println("total="+total);
		System.out.println("accuracy="+df.format(accuracy)+" %");
	}

	//calculating total positive hits in test count
	private static void calculateTestCount(ArrayList<String[]> testList2,String[] split) 
	{
		String trainedAttr[]=split[0].split(":");
		String ans=split[1];
		ArrayList<String[]> testListLocal=new ArrayList<>();
		testListLocal.addAll(testList2);
		int total=testListLocal.size();
		for(int j=0;j<total;j++)
		{
			boolean found=true;
			for(int k=0;k<trainedAttr.length;k++)
			{
				String singleAtt[]=trainedAttr[k].split("-");
				int column=attListMap.get(singleAtt[0]);
				//loop through the list to check if it matches the specified attribute					
				if(!testListLocal.get(j)[column].equalsIgnoreCase(singleAtt[1]))
					found=false;//if not found is false
			}
			if(found)
			{
				String testAns=testListLocal.get(j)[testListLocal.get(j).length-1];
				testList.remove(testListLocal.get(j));
				if(testAns.equalsIgnoreCase(ans))//check if the target in test and the tree target value are the same
				{
					testCounter++;//increment counter to calculate the positive hits
				}
			}
		}
	}

	//calculating training accuracy
	public static void calculateTrainAccuracy(ArrayList<String[]> trainList)
	{
		int total=trainList.size();
		for(int i=0;i<completedTree.size();i++)
		{
			String split[]=completedTree.get(i).split("-->");//split based on --> so at index 1 the split function will store the target value
			calculateTrainCount(trainList,split);

		}

		double accuracy=((double)trainCounter/(double)total)*100;//accuracy =total hits/the no of data*100
		System.out.println("accuracy="+df.format(accuracy)+" %");
	}

	//calculating total positive hits in training set
	private static void calculateTrainCount(ArrayList<String[]> trainList2,String[] split) 
	{
		String trainedAttr[]=split[0].split(":");
		String ans=split[1];
		ArrayList<String[]> trainListLocal=new ArrayList<>();
		trainListLocal.clear();
		trainListLocal.addAll(trainList2);
		int total=trainListLocal.size();
		for(int j=0;j<total;j++)
		{
			boolean found=true;
			for(int k=0;k<trainedAttr.length;k++)
			{
				String singleAtt[]=trainedAttr[k].split("-");
				int column=attListMap.get(singleAtt[0]);
				//loop through the list to check if it matches the specified attribute					
				if(!trainListLocal.get(j)[column].equalsIgnoreCase(singleAtt[1]))
					found=false;//if not found is false
			}
			if(found)
			{
				String testAns=trainListLocal.get(j)[trainListLocal.get(j).length-1];
				trainList.remove(trainListLocal.get(j));
				if(testAns.equalsIgnoreCase(ans))//check if the target in test and the tree target value are the same
				{
					trainCounter++;//increment counter to calculate the positive hits
				}
			}
		}
	}


	//	clearGlaobalLists();
	public static void clearGlaobalLists()
	{
		attListMap.clear();
		attListMapData.clear();
		attrList.clear();
		mainDataList.clear();
		testList.clear();
		removeFromFinalTreeData="";
		treeData.clear();
		completedTree.clear();
		trainList.clear();
		testList.clear();
		mainDataList.clear();


	}
}
