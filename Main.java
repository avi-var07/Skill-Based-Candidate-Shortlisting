import java.util.*;

class Candidate {
    String name;
    String email;
    Map<String, Integer> skills;
    int experience;
    
    Candidate(String name, String email, int experience) {
        this.name = name;
        this.email = email;
        this.experience = experience;
        this.skills = new HashMap<>();
    }
    
    public void addSkill(String skillName, int level) {
        skills.put(skillName.toLowerCase().trim(), level);
    }
    
    public boolean hasSkill(String skillName) {
        return skills.containsKey(skillName.toLowerCase().trim());
    }
    
    public int getSkillLevel(String skillName) {
        return skills.getOrDefault(skillName.toLowerCase().trim(), 0);
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getExperience() { return experience; }
    public Map<String, Integer> getSkills() { return skills; }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %d years exp - Skills: %s", 
                           name, email, experience, skills);
    }
}

class JobRequirement {
    String skillName;
    int requiredLevel;
    double weight;
    
    JobRequirement(String skillName, int requiredLevel, double weight) {
        this.skillName = skillName.toLowerCase().trim();
        this.requiredLevel = requiredLevel;
        this.weight = weight;
    }
    
    public String getSkillName() { return skillName; }
    public int getRequiredLevel() { return requiredLevel; }
    public double getWeight() { return weight; }
    
    @Override
    public String toString() {
        return String.format("%s (Level: %d, Weight: %.1f)", 
                           skillName, requiredLevel, weight);
    }
}


class ScoredCandidate implements Comparable<ScoredCandidate> {
    Candidate candidate;
    double score;
    //sort on basis of score
    public ScoredCandidate(Candidate candidate, double score) {
        this.candidate = candidate;
        this.score = score;
    }
    
    @Override
    public int compareTo(ScoredCandidate other) {
        return Double.compare(other.score, this.score); // Higher score first
    }
    
    public Candidate getCandidate() { return candidate; }
    public double getScore() { return score; }
}


class SkillBasedShortlistingSystem {
    List<Candidate> candidates;     //store list of all candidates
    Scanner sc = new Scanner(System.in);
    
    SkillBasedShortlistingSystem() {
        this.candidates = new ArrayList<>();
        
    }
    
    // Add candidate through user input
    public void addCandidate() {
        System.out.println("\n=== ADD NEW CANDIDATE ===");
        
        System.out.print("Enter candidate's name: ");
        String name = sc.nextLine().trim();
        
        System.out.print("Enter candidate's  email: ");
        String email = sc.nextLine().trim();
        
        System.out.print("Enter years of experience: ");
        int experience = sc.nextInt();
        sc.nextLine(); // consume newline
        
        Candidate candidate = new Candidate(name, email, experience);
        
        System.out.println("Enter skills (type 'done' when finished):");
        while (true) {
            System.out.print("Skill name (or 'done'): ");
            String skillName = sc.nextLine().trim();
            
            if (skillName.equalsIgnoreCase("done")) {
                break;
            }
            
            System.out.print("Skill level (1-5): ");
            int level = sc.nextInt();
            sc.nextLine(); // consume newline
            
            if (level >= 1 && level <= 5) {
                candidate.addSkill(skillName, level);
                System.out.println("Added: " + skillName + " (Level " + level + ")");
            } else {
                System.out.println("Invalid level! Please enter 1-5.");
            }
        }
        
        candidates.add(candidate);
        System.out.println("Candidate added successfully!");
    }
    
    // Get job requirements through user input
    public List<JobRequirement> getJobRequirements() {
        System.out.println("\n=== DEFINE JOB REQUIREMENTS ===");
        List<JobRequirement> requirements = new ArrayList<>();
        
        System.out.println("Enter job requirements (type 'done' when finished):");
        double totalWeight = 0.0;
        
        while (true) {
            System.out.print("Required skill name (or 'done'): ");
            String skillName = sc.nextLine().trim();
            
            if (skillName.equalsIgnoreCase("done")) {
                break;
            }
            
            System.out.print("Required level (1-5): ");
            int level = sc.nextInt();
            
            System.out.print("Importance weight (0.1-1.0): ");
            double weight = sc.nextDouble();
            sc.nextLine(); // consume newline
            
            if (level >= 1 && level <= 5 && weight >= 0.1 && weight <= 1.0) {
                requirements.add(new JobRequirement(skillName, level, weight));
                totalWeight += weight;
                System.out.println("Added requirement: " + skillName + " (Level " + level + ", Weight " + weight + ")");
            } else {
                System.out.println("Invalid input! Level should be 1-5, weight should be 0.1-1.0");
            }
        }
        
        System.out.printf("Total weight: %.1f%n", totalWeight);
        return requirements;
    }
    
    // Calculate match score for a candidate
    //if skill present: match = candidateLevel/ requiredLevel, then multiply by weightage. totalScore/totalWeight *100 is the final score.
    private double calculateMatchScore(Candidate candidate, List<JobRequirement> requirements) {
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        for (JobRequirement req : requirements) {
            totalWeight += req.getWeight();
            
            if (candidate.hasSkill(req.getSkillName())) {
                int candidateLevel = candidate.getSkillLevel(req.getSkillName());
                int requiredLevel = req.getRequiredLevel();
                
                // Calculate match percentage
                double matchPercentage = Math.min((double) candidateLevel / requiredLevel, 1.0);
                
                // Bonus for exceeding requirements
                if (candidateLevel > requiredLevel) {
                    matchPercentage = Math.min(1.0 + (candidateLevel - requiredLevel) * 0.1, 1.5);
                }
                
                totalScore += matchPercentage * req.getWeight();
            }
        }
        
        return totalWeight > 0 ? (totalScore / totalWeight) * 100 : 0;
    }
    
    // Shortlist candidates
    public List<ScoredCandidate> shortlistCandidates(List<JobRequirement> requirements, int topN) {
        PriorityQueue<ScoredCandidate> queue = new PriorityQueue<>();
        
        for (Candidate candidate : candidates) {
            double score = calculateMatchScore(candidate, requirements);
            queue.offer(new ScoredCandidate(candidate, score));
        }
        
        List<ScoredCandidate> shortlist = new ArrayList<>();
        int count = Math.min(topN, queue.size());
        
        for (int i = 0; i < count; i++) {
            shortlist.add(queue.poll());
        }
        
        return shortlist;
    }
    
    // Display all candidates
    public void displayAllCandidates() {
        System.out.println("\n=== ALL CANDIDATES ===");
        if (candidates.isEmpty()) {
            System.out.println("No candidates in the system. Either add candidates or choose sample candidates for testing purpose!");
            return;
        }
        
        for (int i = 0; i < candidates.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, candidates.get(i));
        }
    }
    
    // Display shortlisted candidates
    public void displayShortlist(List<ScoredCandidate> shortlist) {
        System.out.println("\n=== SHORTLISTED CANDIDATES ===");
        if (shortlist.isEmpty()) {
            System.out.println("No candidates match the requirements.");
            return;
        }
        
        System.out.println("Rank | Score | Candidate Details");
        System.out.println("-----|-------|------------------");
        
        for (int i = 0; i < shortlist.size(); i++) {
            ScoredCandidate sc = shortlist.get(i);
            System.out.printf("%4d | %5.1f%% | %s%n", 
                            i + 1, sc.getScore(), sc.getCandidate());
        }
    }
    
    // Some sample candidates testing k liye
    public void addSampleCandidates() {
        System.out.println("Adding sample candidates...");
        
    
        Candidate c1 = new Candidate("Aviral Ved Varshney", "aviralvarshney07@gmail.com", 5);
        c1.addSkill("Java", 4);
        c1.addSkill("SpringBoot", 3);
        c1.addSkill("MySQL", 4);
        c1.addSkill("React JS", 3);
        c1.addSkill("Tailwind CSS", 3);
        c1.addSkill("Git", 3);
        candidates.add(c1);
        
        
        Candidate c2 = new Candidate("Jethalal Gada", "jalebi@fafda.com", 3);
        c2.addSkill("CPP", 3);
        c2.addSkill("C", 2);
        c2.addSkill("HTML", 4);
        c2.addSkill("Git", 4);
        candidates.add(c2);
        
        
        Candidate c3 = new Candidate("Patrakar Popatlal Pandey", "cancelCancelCancel@dibba.com", 2);
        c3.addSkill("Django", 2);
        c3.addSkill("Python", 3);
        c3.addSkill("Git", 2);
        candidates.add(c3);
        
    
        
        System.out.println("Sample candidates added successfully!");
    }
    
    
    public void mainMenu() {
        System.out.println("=== SKILL-BASED CANDIDATE SHORTLISTING SYSTEM ===");
        System.out.println("Welcome! This system helps recruiters to shortlist candidates based on their requirements. ");
        
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Add New Candidate");
            System.out.println("2. View All Candidates");
            System.out.println("3. Shortlist Candidates");
            System.out.println("4. Add Sample Candidates");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");
            
            int choice = sc.nextInt();
            sc.nextLine(); 
            
            switch (choice) {
                case 1:
                    addCandidate();
                    break;
                case 2:
                    displayAllCandidates();
                    break;
                case 3:
                    if (candidates.isEmpty()) {
                        System.out.println("No candidates in the system! Please add candidates first.");
                    } else {
                        performShortlisting();
                    }
                    break;
                case 4:
                    addSampleCandidates();
                    break;
                case 5:
                    System.out.println("Thank you for using the Shortlisting System! This system is designed by Aviral Ved Varshney. For any queries contact: aviralvarshney07@gmail.com.");
                    return;
                default:
                    System.out.println("Invalid choice! Please enter between 1-5 as per the options.");
            }
        }
    }
    

    private void performShortlisting() {
        List<JobRequirement> requirements = getJobRequirements();
        
        if (requirements.isEmpty()) {
            System.out.println("No job requirements defined!");
            return;
        }
        
        System.out.print("How many candidates do you want to shortlist? ");
        int topN = sc.nextInt();
        sc.nextLine(); 
        
        List<ScoredCandidate> shortlist = shortlistCandidates(requirements, topN);
        displayShortlist(shortlist);
        
    
        System.out.println("\n=== JOB REQUIREMENTS (for reference) ===");
        for (JobRequirement req : requirements) {
            System.out.println("- " + req);
        }
    }
}

class Main {
    public static void main(String[] args) {
        SkillBasedShortlistingSystem obj = new SkillBasedShortlistingSystem();
        obj.mainMenu();
    }
}