import java.util.* ;
import java.io.* ;


public class Apriori {//Different data structures are declared
	public List<ArrayList<String>> items;
	public List<ArrayList<String>> Fitems;
	public List<ArrayList<String>> transactions;
	public List<ArrayList<String>> candidates;
	public Set<String> One_item;
	public String fileN;
	public int nTrans;
	public int nLength;
	public double min_S;

  //Constructor to read the filename and the minimum support which is 23% in our case
	public Apriori(String fileN, double min_S){
		this.fileN = fileN;
		this.min_S = min_S;
		this.nTrans = 0;
		this.nLength = 0;
		this.items = new ArrayList<ArrayList<String>>();
		this.Fitems = new ArrayList<ArrayList<String>>();
		this.transactions = new ArrayList<ArrayList<String>>();
		this.One_item = new HashSet<String>();
		this.candidates = new ArrayList<ArrayList<String>>();
	}
//Scanner class from the utils is taken to read the fileN
	public void Reading_input_file(){
		try {
			Scanner sc = new Scanner(new File(fileN));
			while (sc.hasNextLine()) {
				String t = sc.nextLine();
				ArrayList<String> transection = new ArrayList<String>();
				nTrans++;
				StringTokenizer st = new StringTokenizer(t, ", ");
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					transection.add(token);
					One_item.add(token);
				}
				transactions.add(transection);
			}
		} catch (FileNotFoundException e) {
			System.err.println("couldn't read file: " + fileN);
		}
	}
//First level to generate the frequentItemSet1 with minimumSupport threshold
	public ArrayList<ArrayList<String>> findFrequent1(){
		ArrayList<ArrayList<String>>  frequentItemSet1 = new ArrayList<ArrayList<String>>();
		for(String s: One_item){
			ArrayList<String> a = new ArrayList<String>();
			int count = 0;
			for(ArrayList<String> t: transactions){
				if(t.contains(s)){
					count++;
				}
			}
			if(count >= nTrans*min_S){
				a.add(s);
				frequentItemSet1.add(a);
			}
		}
		return frequentItemSet1;
	}

	public void gen_candidate_sets(){
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		if(nLength == 1){
			temp = findFrequent1();
		}else{
			String s = null;
			for(int i = 0; i < items.size(); i ++){
				for(int j = i; j < items.size(); j ++){
					int ndiff = 0;
					ArrayList<String> tempCandidate = new ArrayList<String>();
					for(int k = 0; k < items.get(j).size(); k++){
						if(!items.get(i).contains(items.get(j).get(k))){
							ndiff++;
							s = items.get(j).get(k);
						}
					}
					if(ndiff == 1){
						tempCandidate.addAll(items.get(i));
						tempCandidate.add(s);
						temp.add(tempCandidate);
					}
				}
			}
		}
		candidates = temp;
	}

	public void prune(){
		ArrayList<ArrayList<String>> tempitems = new ArrayList<ArrayList<String>>();
		if(nLength > 1){
			for(ArrayList<String> i: candidates){
				boolean frequent = true;
				for(int j = 0; j < i.size(); j++){
					ArrayList<String> temp = new ArrayList<String>();
					temp.addAll(i);
					i.remove(j);
					if(!items.contains(i)){
						frequent = false;
					}
					i = temp;
				}
				if(frequent){
					tempitems.add(i);
				}
			}
			candidates = tempitems;
		}
	}

	public void implement(){
		Reading_input_file();
		nLength = 1;
		do{
			gen_candidate_sets();
			prune();
			items.clear();
			for(ArrayList<String> a: candidates){
				int count = 0;
				for(ArrayList<String> transection: transactions){
					boolean contain = true;
					for(String s: a){
						if(!transection.contains(s)){
							contain = false;
							break;
						}
					}
					if(contain){
						count++;
					}
				}
				if(count >= nTrans * min_S && !items.contains(a)){
					items.add(a);
				}
			}
			Fitems.addAll(items);
			nLength++;
		}while(items.size() > 0);

	}

	public void printfunction(){
		for(ArrayList<String> a: Fitems){
			System.out.print("The frequent itemsets:");
			System.out.println();
			System.out.print("{");
			for(String s: a){
				System.out.print(s + ", ");
			}
			System.out.print("}");
			System.out.println();
		}
	}

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		Apriori apriori = new Apriori("adult.txt", 0.6);
		apriori.implement();
		apriori.printfunction();
		long endTime   = System.nanoTime();
                long totalTime = endTime - startTime;
                long timeinsec= totalTime/1000000000;
                System.out.println("The runtime of the algorithm:" +timeinsec);

	}
}
