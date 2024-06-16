// Import library
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class ID3 {
    // Deklarasi variabel yang akan digunakan
    private String used; //status atribut yang sudah digunakan
    private int attributes; //jumlah atribut
    private int examples; //jumlah contoh
    private TreeNode decisionTree; //tree dari decision tree, yang digunakan untuk klasifikasi
    private TheArrayList<TheArrayList<String>> data; //data train
    private TheArrayList<TheArrayList<String>> strings; //nilai nilai unik dari atribut
    private TheArrayList<Integer> stringCount; //jumlah nilai unik setiap atribut

	/** Setiap node dari tree berisi nomor atribut (untuk node bukan daun) atau //nomor kelas (untuk node daun) dalam <b>value</b>, dan array dari //node tree dalam <b>children</b> yang berisi setiap anak dari node tersebut (untuk node bukan daun).
	 *Nomor atribut sesuai dengan nomor kolom dalam file pelatihan dan pengujian. Anak-anak diurutkan dalam urutan yang sama dengan Strings dalam strings[][].
**/

    class TreeNode {
        TreeNode[] children; //array children dari node
        int value; // nilai atribute atau kelas dari node

        // konstruktor
        public TreeNode(TreeNode[] ch, int val) {
            value = val;
            children = ch;
        }

        // mengubah node ke string
        public String toString() {
            return toString("");
        }

        // mengubah node ke string dengan indent
        String toString(String indent) {
            if (children != null) {
                String s = "";
                for (int i = 0; i < children.length; i++)
                    s += indent + data.get(0).get(value) + "=" +
                         strings.get(value).get(i) + "\n" +
                         children[i].toString(indent + '\t');
                return s;
            } else
                return indent + "Class: " + strings.get(attributes-1).get(value) + "\n";
        }
    }

    // konstruktor untuk inisialisasi ID3
    public ID3() {
        used = "used";
        attributes = 0;
        examples = 0;
        decisionTree = null;
        data = new TheArrayList<>(600); // Sesuaikan ukuran maksimal sesuai kebutuhan
        strings = new TheArrayList<>(600); // Sesuaikan ukuran maksimal sesuai kebutuhan
        stringCount = new TheArrayList<>(600); // Sesuaikan ukuran maksimal sesuai kebutuhan
    }

    // Fungsi untuk mencetak decision tree
    public void printTree() {
        if (decisionTree == null)
            error("Attempted to print null Tree");
        else
            System.out.println(decisionTree);
    }

    // Fungsi untuk mencetak pesan error
    static void error(String msg) {
        System.err.println("Error: " + msg);
        System.exit(1);
    }

    // Mendefinisikan variabel konstan
    static final double LOG2 = Math.log(2.0);

    // Fungsi untuk menghitung x log x basis 2
    static double xlogx(double x) {
        return x == 0? 0: x * Math.log(x) / LOG2;
    }

    /**
     Fungsi untuk menjalankan pohon keputusan pada contoh-contoh yang diberikan dalam testData, dan mencetak nama kelas hasil, satu per baris, untuk setiap contoh dalam testData
     */
    // Fungsi untuk mengklasifikasikan data uji dengan menggunakan decision tree
    public void classify(TheArrayList<TheArrayList<String>> testData) {
        if (decisionTree == null)
            error("Please run training phase before classification");
        
            for(int i = 0; i < testData.size(); i++) {
                String ans = transverse(decisionTree, testData.get(i));
                if (ans.equalsIgnoreCase("No")) {
                    System.out.println("Hasil prediksi:\nAnda tidak mengidap Diabetes.");
                } else if (ans.equalsIgnoreCase("Yes")) {
                    System.out.println("Hasil prediksi:\nAnda mengidap Diabetes.");
                } 
            }
    }


    // Fungsi untuk menelusuri decision tree berdasarkan nilai atribut dalam row dan mangambil hasil class yang diprediksi
    public String transverse(TreeNode currentNode, TheArrayList<String> row) {
        if (currentNode.children == null) {
            return strings.get(attributes-1).get(currentNode.value);
        } else {
            int posInStrings = -1;
            for (int i = 0; i < strings.get(currentNode.value).size(); i++) {
                if (row.get(currentNode.value).equals(strings.get(currentNode.value).get(i))) {
                    posInStrings = i;
                    break;
                }
            }

            if (posInStrings == -1) {
                throw new IllegalArgumentException("Invalid attribute value: " + row.get(currentNode.value));
            }

            return transverse(currentNode.children[posInStrings], row);
        }
    }

    // Fungsi untuk melatih model ID3 dengan data latih
    public void train(TheArrayList<TheArrayList<String>> trainingData) {
        indexStrings(trainingData);
        TheArrayList<String> usedAttributes = data.get(0);
        decisionTree = new TreeNode(null, 0);
        buildTree(decisionTree, trainingData, usedAttributes);
    }

    // Fungsi untuk memeriksa apakah semua atribut sudah digunakanj
    boolean checkUsedAttributes(TheArrayList<String> attrCol) {
        int attrCounter = 0;
        for(int i = 0; i < attrCol.size() - 1; i++) {
            if(attrCol.get(i).equals(used)){
                attrCounter++;
            }
        }
        return attrCounter == attrCol.size() - 1;
    }

    // Fungsi untuk mendapatkan subset data berdasarkan nilai atribut
    public TheArrayList<TheArrayList<String>> getSubset(TheArrayList<TheArrayList<String>> currentDataSet, int attr, int attrVal) {
        int attrCounter = countAttributes(currentDataSet, attr, attrVal);
        TheArrayList<TheArrayList<String>> subSet = new TheArrayList<>(attrCounter+1);
        subSet.add(currentDataSet.get(0));
        for (int i = 1; i < currentDataSet.size(); i++) {
            if (currentDataSet.get(i).get(attr).equals(strings.get(attr).get(attrVal))) {
                subSet.add(currentDataSet.get(i));
            }
        }
        return subSet;
    }

    // Fungsi untuk membangun decision tree secara rekursif
    public void buildTree(TreeNode node, TheArrayList<TheArrayList<String>> currentDataSet, TheArrayList<String> usedAttributes){
        double rootEntropy = calcEntropy(currentDataSet); ;// menghitung entropu dari dataset saat ini
        double rows = examples-1;
        double comparator = 0; // menyimpan nilai information gain tertinggi
        int bestAttribute = 0; // menyimpan indekx atribut dengan information gain tertinggi
        double[] infoGain = new double[attributes]; // meyimpan information gain dari setiap atribut
        double[] subSetEntropy; // meyimpan entropy dari setiap subset data
        double[] instanceCount; // menyimpan jumlah instance dari setiap subset data

        // jika entropy == 0 maka akan jadi leaf node
        if (rootEntropy <= 0.0 || checkUsedAttributes(usedAttributes)) {
            int leafClass = 0;
            int instances = 0;
            for (int z = 0; z < stringCount.get(attributes-1); z++) {
                if (instances < countAttributes(currentDataSet, currentDataSet.get(0).size()-1, z)) {
                    instances = countAttributes(currentDataSet, currentDataSet.get(0).size()-1, z);
                    leafClass = z;
                }
            }
            node.value = leafClass;
            return;
            //  menghitung entropy untuk setiap subset data
        } else {
            for (int i = 0; i < currentDataSet.get(0).size()-1; i++) {
                if (usedAttributes.get(i).equals(used)) {
                    infoGain[i] = 0;
                } else {
                    subSetEntropy = new double[stringCount.get(i)];
                    instanceCount = new double[stringCount.get(i)];
                    for (int j = 0; j < stringCount.get(i); j++) {
                        TheArrayList<TheArrayList<String>> subSet = getSubset(currentDataSet, i, j);
                        subSetEntropy[j] = calcEntropy(subSet);
                        instanceCount[j] = countAttributes(subSet, i, j);
                    }
                    infoGain[i] = rootEntropy;
                    double tmp = 0;
                    for (int a = 0; a < subSetEntropy.length; a++) {
                        tmp = (instanceCount[a]/rows*subSetEntropy[a]);
                        if (!Double.isNaN(tmp)) {
                            infoGain[i] -= tmp;
                        }
                    }
                    infoGain[i] = Math.abs(infoGain[i]);
                    if (infoGain[i] >= comparator && !usedAttributes.get(i).equals(used)) {
                        comparator = infoGain[i];
                        bestAttribute = i;
                    }
                }
            }
            node.value = bestAttribute;
            node.children = new TreeNode[stringCount.get(bestAttribute)];

            for (int n = 0; n < stringCount.get(bestAttribute); n++) {
                TheArrayList<String> temp = new TheArrayList<>(usedAttributes.size());
                for (int i = 0; i < usedAttributes.size(); i++) {
                    temp.add(usedAttributes.get(i));
                }
                TheArrayList<TheArrayList<String>> newSubSet = getSubset(currentDataSet, bestAttribute, n);
                node.children[n] = new TreeNode(null, 0);
                if (newSubSet.size() != 1) {
                    temp.set(bestAttribute, used);
                    buildTree(node.children[n], newSubSet, temp);
                } else {
                    for (int m = 0; m < temp.size()-1; m++) {
                        temp.set(m, used);
                    }
                    buildTree(node.children[n], currentDataSet, temp);
                }
            }
        }
    }

    public int countAttributes(TheArrayList<TheArrayList<String>> currentDataSet, int attr, int attrVal) {
        int count = 0;
        if (currentDataSet.size() == 1) {
            return count;
        }
        for (int i = 1; i < currentDataSet.size(); i++) {
            if (currentDataSet.get(i).get(attr).equals(strings.get(attr).get(attrVal))) {
                count++;
            }
        }
        return count;
    }

    public double calcEntropy(TheArrayList<TheArrayList<String>> currentDataSet) {
        double rows = currentDataSet.size()-1;
        double[] noClassInstances = new double[stringCount.get(attributes-1)];
        for (int i = 0; i < stringCount.get(attributes-1); i++) {
            noClassInstances[i] = countAttributes(currentDataSet, attributes-1, i);
        }
        double entropy = -xlogx(noClassInstances[0]/rows);
        for (int a = 1; a < noClassInstances.length; a++) {
            entropy += -xlogx(noClassInstances[a]/rows);
        }
        return entropy;
    }

    public void indexStrings(TheArrayList<TheArrayList<String>> trainingData) {
        data = trainingData;
        attributes = trainingData.get(0).size();
        examples = trainingData.size();
        TheArrayList<TheArrayList<String>> temp = new TheArrayList<>(attributes);
        for (int i = 0; i < attributes; i++) {
            TheArrayList<String> attrList = new TheArrayList<>(600); // Sesuaikan ukuran maksimal sesuai kebutuhan
            attrList.add(trainingData.get(0).get(i));
            temp.add(attrList);
        }
        strings = temp;

        for (int i = 1; i < examples; i++) {
            for (int j = 0; j < attributes; j++) {
                TheArrayList<String> attrList = strings.get(j);
                boolean found = false;
                for (int k = 0; k < attrList.size(); k++) {
                    if (attrList.get(k).equals(trainingData.get(i).get(j))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    attrList.add(trainingData.get(i).get(j));
                }
            }
        }

        stringCount = new TheArrayList<>(attributes);
        for (int i = 0; i < attributes; i++) {
            stringCount.add(strings.get(i).size());
        }
    }

    public static void main(String[] args) {
        ID3 id3 = new ID3();
        TheArrayList<TheArrayList<String>> trainingData = new TheArrayList<>(600); // Adjust size as needed
    
        try (BufferedReader br = new BufferedReader(new FileReader("train_data.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                TheArrayList<String> row = new TheArrayList<>(600); // Adjust size as needed
                String[] splitLine = line.split(",");
                for (String s : splitLine) {
                    row.add(s);
                }
                trainingData.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        id3.train(trainingData);
        id3.printTree();
    
        Scanner scanner = new Scanner(System.in);
        TheArrayList<String> testInstance = new TheArrayList<>(id3.attributes);
        System.out.println("Glucose \t\t: (<= 140 : Baik) (141-199 : Sedang) (>= 200 Buruk)");
        System.out.println("Bloodpressure \t\t: (<=80 : Normal) (81-89 : Pre-Hipertensi) (>= 90 : Hipertensi)");
        System.out.println("BMI\t\t\t: (<=18.5 : Kurang) (18.6 - 29.9 : Normal) (>30 : Obese)");
        System.out.println("Dibetes Pedigree\t: (<= 0.69 : Aman) (0.70 - 1.49 : Waspada) (>= 1.5 : Bahaya)");
        System.out.println("Age\t\t\t: (<= 20 : Remaja) (21-59 : Dewasa) (>= 60 : Lansia)");
        
        System.out.println("Enter numeric values for the following attributes:");
        System.out.print("Glucose: ");
        double glucose = Integer.parseInt(scanner.nextLine().trim());
        String glucoseCategory = glucose <= 140 ? "Baik" : (glucose <= 199 ? "Sedang" : "Buruk");
        testInstance.add(glucoseCategory);
    
        System.out.print("Blood Pressure: ");
        double bloodPressure = Integer.parseInt(scanner.nextLine().trim());
        String bpCategory = bloodPressure <= 80 ? "Normal" : (bloodPressure <= 89 ? "Pre-Hipertensi" : "Hipertensi");
        testInstance.add(bpCategory);
    
        System.out.print("BMI: ");
        double bmi = Double.parseDouble(scanner.nextLine().trim());
        String bmiCategory = bmi <= 18.5 ? "Kurang" : (bmi <= 29.9 ? "Normal" : "Obese");
        testInstance.add(bmiCategory);
    
        System.out.print("Diabetes Pedigree: ");
        double diabetesPedigree = Double.parseDouble(scanner.nextLine().trim());
        String dpCategory = diabetesPedigree <= 0.69 ? "Aman" : (diabetesPedigree <= 1.49 ? "Waspada" : "Bahaya");
        testInstance.add(dpCategory);
    
        System.out.print("Age: ");
        int age = Integer.parseInt(scanner.nextLine().trim());
        String ageCategory = age <= 20 ? "Remaja" : (age <= 59 ? "Dewasa" : "Lansia");
        testInstance.add(ageCategory);
    
        TheArrayList<TheArrayList<String>> testData = new TheArrayList<>(1);
        testData.add(testInstance);
    
        id3.classify(testData);
        scanner.close();
    }
}