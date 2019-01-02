import java.io.* ;
import java.util.* ;


public class FPGrowth {

	public ArrayList<HeaderN> headerTable;
	public FPTree fpTree;
	public List<ArrayList<String>> transactions;
	public String fileN;
	public int trans_n;
	public HashSet<ArrayList<String>> frequentpatterns;
	public Hashtable<String, Integer> item_count;
  public double min_S;
	public FPGrowth(String fileN, double min_S){
		this.fileN = fileN;
		this.min_S = min_S;
		this.trans_n = 0;
		this.headerTable = new ArrayList<HeaderN>();
		this.transactions = new ArrayList<ArrayList<String>>();
		this.frequentpatterns = new HashSet<ArrayList<String>>();
		this.item_count = new Hashtable<String, Integer>();
	}

	public void readFile(){
		try {
			Scanner scan = new Scanner(new File(fileN));
			while (scan.hasNextLine()) {
				String t = scan.nextLine();
				ArrayList<String> transaction = new ArrayList<String>();
				trans_n++;
				StringTokenizer st = new StringTokenizer(t, ", ");
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					transaction.add(token);
					if(item_count.containsKey(token)){
						item_count.put(token, item_count.get(token) + 1);
					}else{
						item_count.put(token, 1);
					}
				}
				transactions.add(transaction);
			}
		} catch (FileNotFoundException e) {
			System.err.println("couldn't read file: " + fileN);
		}
	}

	public ArrayList<HeaderN> createHeaderTable(){
		ArrayList<HeaderN> headerTable = new ArrayList<HeaderN>();
		for(String s: item_count.keySet()){
			if(item_count.get(s) >= trans_n*min_S){
				headerTable.add(new HeaderN(s, item_count.get(s)));
			}
		}
		Collections.sort(headerTable, new HeaderComparator());
		return headerTable;
	}

	public void second_scan(){
		fpTree = new FPTree("null");
		fpTree.root = true;
		fpTree.itemID = null;
		for(ArrayList<String> transaction: transactions){
			ArrayList<String> ord_transaction = new ArrayList<String>();
			for(HeaderN h: headerTable){
				if(transaction.contains(h.itemID)){
					ord_transaction.add(h.itemID);
				}
			}
			insert_into(ord_transaction, fpTree, 0);
		}
	}

	public void insert_into(ArrayList<String> ord_transaction, FPTree fpTree, int n){//algorithm from my open source "FP-Growth"
		if(n < ord_transaction.size()){
			String item = ord_transaction.get(n);
			FPTree new_Tree = null;
			boolean found = false;
			for(FPTree child: fpTree.children){
				if(child.itemID.equals(item)){
					new_Tree = child;
					child.count++;
					found = true;
					break;
				}
			}
			if(!found){
				new_Tree = new FPTree(item);
				new_Tree.count = 1;
				new_Tree.parent = fpTree;
				fpTree.children.add(new_Tree);
				for(HeaderN h: headerTable){
					if(h.itemID.equals(item)){
						FPTree temp = h.nodeLink;
						if(temp == null){
							h.nodeLink = new_Tree;
						}else{
							while(temp.next != null){
								temp = temp.next;
							}
							temp.next = new_Tree;
						}
					}
				}
			}
			insert_into(ord_transaction, new_Tree, n+1);
		}
	}

	public void growth(FPTree fpTree, ArrayList<String> check, ArrayList<HeaderN> headerTable){
		if(is_single_or_not(fpTree)){
			ArrayList<String> items = new ArrayList<String>();
			while(fpTree != null){
				if(fpTree.itemID != null){
					items.add(fpTree.itemID);
				}
				if(fpTree.children.size() > 0){
					fpTree = fpTree.children.get(0);
				}else{
					fpTree = null;
				}
			}
			ArrayList<ArrayList<String>> combinations = generateCombinations(items, headerTable);
			for(ArrayList<String> combination: combinations){
				combination.addAll(check);
			}
			frequentpatterns.addAll(combinations);
		}else{
			for(int i = headerTable.size() - 1; i >=0; i--){
				ArrayList<String> combination = new ArrayList<String>();
				combination.addAll(check);
				combination.remove(null);
				combination.add(headerTable.get(i).itemID);
				int count = headerTable.get(i).supportCount;
				frequentpatterns.add(combination);
				ArrayList<ArrayList<String>> conPatBaseTrans = new ArrayList<ArrayList<String>>();
				FPTree temp = headerTable.get(i).nodeLink;
				HashSet<String> newOneItems = new HashSet<String>();
				while(temp != null){
					FPTree temp2 = temp;
					ArrayList<String> newItemset = new ArrayList<String>();
					while(temp.itemID != null){
						if(temp != temp2){
							newItemset.add(temp.itemID);
							newOneItems.add(temp.itemID);
						}
						temp = temp.parent;
					}
					for(int j = 0; j < temp2.count; j++){
						conPatBaseTrans.add(newItemset);
					}
					temp = temp2.next;
				}
				ArrayList<HeaderN> newHeaderTable = c_HT_Sub(conPatBaseTrans, newOneItems);
				FPTree new_Tree = second_scan_sub(conPatBaseTrans, newHeaderTable);
				if(new_Tree.children.size() > 0){
					growth(new_Tree, combination, newHeaderTable);
				}
			}
		}
	}

	public boolean is_single_or_not(FPTree fpTree){
		boolean is_single_or_not = true;
		if(fpTree.children.size() > 1){
			is_single_or_not = false;
			return is_single_or_not;
		}else{
			for(FPTree child: fpTree.children){
				if(is_single_or_not){
					is_single_or_not = is_single_or_not(child);
				}else{
					break;
				}
			}
		}
		return is_single_or_not;
	}

	public ArrayList<ArrayList<String>> generateCombinations(ArrayList<String> items, ArrayList<HeaderN> newHeaderTable){//algorithm from my open source "Permutation"
		ArrayList<ArrayList<String>> combinations = new ArrayList<ArrayList<String>>();
		if(items.size() != 0){
			String s = items.get(0);
			if(items.size() > 1){
				items.remove(0);
				ArrayList<ArrayList<String>> combinationsSub = generateCombinations(items, newHeaderTable);
				combinations.addAll(combinationsSub);
				for(ArrayList<String> a: combinationsSub){
					for(int i = 0; i < a.size(); i++){
						ArrayList<String> combination = new ArrayList<String>();
						int count = Integer.MAX_VALUE;
						for(int j = 0; j <= i; j++){
							for(HeaderN h: newHeaderTable){
								if(h.itemID.equals(a.get(j)) && count < h.supportCount){
									count = h.supportCount;
								}
							}
							combination.add(a.get(j));
						}
						for(HeaderN h: newHeaderTable){
							if(h.itemID.equals(s) && count < h.supportCount){
								count = h.supportCount;
							}
						}
						if(count >= min_S*trans_n){
							combination.add(s);
							combinations.add(combination);
						}
					}
				}
			}
			ArrayList<String> combination = new ArrayList<String>();
			combination.add(s);
			combinations.add(combination);
		}
		return combinations;
	}

	public ArrayList<HeaderN> c_HT_Sub(ArrayList<ArrayList<String>> conPatBaseTrans, HashSet<String> newOneItems){
		ArrayList<HeaderN> headerTable = new ArrayList<HeaderN>();
		for(String s: newOneItems){
			int count = 0;
			for(ArrayList<String> t: conPatBaseTrans){
				if(t.contains(s)){
					count++;
				}
			}
			if(count >= trans_n * min_S){
				headerTable.add(new HeaderN(s, count));
			}
		}
		Collections.sort(headerTable, new HeaderComparator());
		return headerTable;
	}

	public FPTree second_scan_sub(ArrayList<ArrayList<String>> conPatBaseTrans, ArrayList<HeaderN> newHeaderTable){
		FPTree fpTreeSub = new FPTree("null");
		fpTreeSub.root = true;
		fpTreeSub.itemID = null;
		for(ArrayList<String> transaction: conPatBaseTrans){
			ArrayList<String> ord_transaction = new ArrayList<String>();
			for(HeaderN h: newHeaderTable){
				if(transaction.contains(h.itemID)){
					ord_transaction.add(h.itemID);
				}
			}
			insert_intoSub(ord_transaction, fpTreeSub, 0, newHeaderTable);
		}
		return fpTreeSub;
	}

	public void insert_intoSub(ArrayList<String> ord_transaction, FPTree fpTree, int n, ArrayList<HeaderN> newHeaderTable){//algorithm from my open source "FP-Growth"
		if(n < ord_transaction.size()){
			String item = ord_transaction.get(n);
			FPTree new_Tree = null;
			boolean found = false;
			for(FPTree child: fpTree.children){
				if(child.itemID.equals(item)){
					new_Tree = child;
					child.count++;
					found = true;
					break;
				}
			}
			if(!found){
				new_Tree = new FPTree(item);
				new_Tree.count = 1;
				new_Tree.parent = fpTree;
				fpTree.children.add(new_Tree);
				for(HeaderN h: newHeaderTable){
					if(h.itemID.equals(item)){
						FPTree temp = h.nodeLink;
						if(temp == null){
							h.nodeLink = new_Tree;
						}else{
							while(temp.next != null){
								temp = temp.next;
							}
							temp.next = new_Tree;
						}
					}
				}
			}
			insert_intoSub(ord_transaction, new_Tree, n+1, newHeaderTable);
		}
	}

	public void implement(){
		readFile();
		headerTable = createHeaderTable();//first scan
		second_scan();
		ArrayList<String> nullList = new ArrayList<String>();
		nullList.add(null);
		growth(fpTree, nullList, headerTable);
	}

	public void printfunction(){
		for(ArrayList<String> a: frequentpatterns){
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
		FPGrowth fpg = new FPGrowth("adult.txt", 0.23);
		fpg.implement();
		fpg.printfunction();
		long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    long timeinsec= totalTime/1000000000;
    System.out.println("The runtime of the algorithm:" +timeinsec);

	}
}
