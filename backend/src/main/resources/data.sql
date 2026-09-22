-- Seed data. Uses guarded INSERT ... SELECT ... WHERE NOT EXISTS so it is safe to
-- run on every startup (spring.sql.init.mode=always) without creating duplicates.

-- Default admin account: username "admin", password "Admin@123" (change immediately in production)
INSERT INTO users (username, email, password_hash, role, created_at, current_streak)
SELECT 'admin', 'admin@codingplatform.local', '$2b$10$m3OLjwq4IVVt6NmKl5INLeu0qQqbyRn0OLXsBKj4WvyKf9Bi0aIe.', 'ADMIN', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- ---------------------------------------------------------------------------
-- Problem 1: Two Sum (EASY)
-- ---------------------------------------------------------------------------
INSERT INTO problems (title, description, difficulty, hints, editorial, unlock_editorial_after_failures,
                       starter_code_java, starter_code_python, starter_code_cpp, starter_code_javascript,
                       time_limit_ms, memory_limit_mb, created_at)
SELECT
 'Two Sum',
 '## Two Sum\n\nGiven an array of integers `nums` and an integer `target`, return the indices of the two numbers such that they add up to `target`.\n\n**Input format:** first line has the array as space-separated integers, second line has the target.\n**Output format:** the two 0-based indices, space-separated, in ascending order.\n\n### Example\nInput:\n```\n2 7 11 15\n9\n```\nOutput:\n```\n0 1\n```',
 'EASY',
 '[\"Try a brute-force nested loop first.\", \"Can you use a hash map to remember numbers you have already seen?\"]',
 'Use a HashMap<value, index>. For each number, check if target - number was already seen; if so, output the stored index and the current index.',
 3,
 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        String[] parts = sc.nextLine().trim().split(\"\\\\s+\");\n        int[] nums = new int[parts.length];\n        for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n        int target = Integer.parseInt(sc.nextLine().trim());\n        // TODO: write your solution\n    }\n}\n',
 'nums = list(map(int, input().split()))\ntarget = int(input())\n# TODO: write your solution\n',
 '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // TODO: write your solution\n    return 0;\n}\n',
 'const lines = require(\"fs\").readFileSync(0, \"utf8\").split(\"\\n\");\nconst nums = lines[0].trim().split(/\\s+/).map(Number);\nconst target = parseInt(lines[1]);\n// TODO: write your solution\n',
 2000, 256, NOW()
WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Two Sum');

INSERT INTO problem_tags (problem_id, tag)
SELECT p.id, t.tag FROM problems p
JOIN (SELECT 'Array' AS tag UNION SELECT 'Hash Table') t
WHERE p.title = 'Two Sum'
  AND NOT EXISTS (SELECT 1 FROM problem_tags pt WHERE pt.problem_id = p.id AND pt.tag = t.tag);

INSERT INTO test_cases (problem_id, input, expected_output, is_hidden, display_order)
SELECT p.id, tc.input, tc.expected_output, tc.is_hidden, tc.display_order
FROM problems p
JOIN (
  SELECT '2 7 11 15\n9' AS input, '0 1' AS expected_output, FALSE AS is_hidden, 0 AS display_order
  UNION ALL SELECT '3 2 4\n6', '1 2', FALSE, 1
  UNION ALL SELECT '3 3\n6', '0 1', TRUE, 2
  UNION ALL SELECT '1 5 3 9 -2 8\n7', '2 5', TRUE, 3
) tc
WHERE p.title = 'Two Sum'
  AND NOT EXISTS (SELECT 1 FROM test_cases t WHERE t.problem_id = p.id AND t.display_order = tc.display_order);

-- ---------------------------------------------------------------------------
-- Problem 2: Reverse String (EASY)
-- ---------------------------------------------------------------------------
INSERT INTO problems (title, description, difficulty, hints, editorial, unlock_editorial_after_failures,
                       starter_code_java, starter_code_python, starter_code_cpp, starter_code_javascript,
                       time_limit_ms, memory_limit_mb, created_at)
SELECT
 'Reverse String',
 '## Reverse String\n\nRead a single line string and print it reversed.\n\n### Example\nInput:\n```\nhello\n```\nOutput:\n```\nolleh\n```',
 'EASY',
 '[\"You can build the answer from the last character to the first.\"]',
 'Read the whole line, then print it iterating from the last character to the first (or use each language''s built-in reverse).',
 2,
 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        String s = sc.nextLine();\n        // TODO: write your solution\n    }\n}\n',
 's = input()\n# TODO: write your solution\n',
 '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    string s;\n    getline(cin, s);\n    // TODO: write your solution\n    return 0;\n}\n',
 'const s = require(\"fs\").readFileSync(0, \"utf8\").split(\"\\n\")[0];\n// TODO: write your solution\n',
 2000, 256, NOW()
WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Reverse String');

INSERT INTO problem_tags (problem_id, tag)
SELECT p.id, 'Strings' FROM problems p
WHERE p.title = 'Reverse String'
  AND NOT EXISTS (SELECT 1 FROM problem_tags pt WHERE pt.problem_id = p.id AND pt.tag = 'Strings');

INSERT INTO test_cases (problem_id, input, expected_output, is_hidden, display_order)
SELECT p.id, tc.input, tc.expected_output, tc.is_hidden, tc.display_order
FROM problems p
JOIN (
  SELECT 'hello' AS input, 'olleh' AS expected_output, FALSE AS is_hidden, 0 AS display_order
  UNION ALL SELECT 'CodingPlatform', 'mroftalPgnidoC', FALSE, 1
  UNION ALL SELECT 'a', 'a', TRUE, 2
) tc
WHERE p.title = 'Reverse String'
  AND NOT EXISTS (SELECT 1 FROM test_cases t WHERE t.problem_id = p.id AND t.display_order = tc.display_order);

-- ---------------------------------------------------------------------------
-- Problem 3: Nth Fibonacci (MEDIUM)
-- ---------------------------------------------------------------------------
INSERT INTO problems (title, description, difficulty, hints, editorial, unlock_editorial_after_failures,
                       starter_code_java, starter_code_python, starter_code_cpp, starter_code_javascript,
                       time_limit_ms, memory_limit_mb, created_at)
SELECT
 'Nth Fibonacci Number',
 '## Nth Fibonacci Number\n\nGiven an integer `n` (0-indexed, F(0)=0, F(1)=1), print F(n) modulo 1000000007.\n\n### Example\nInput:\n```\n10\n```\nOutput:\n```\n55\n```',
 'MEDIUM',
 '[\"A naive recursive solution is exponential — think iteratively or memoize.\", \"You only need the last two values at any point.\"]',
 'Iterate from 0 to n keeping only the last two Fibonacci values, updating them each step, and take mod 1000000007 to avoid overflow.',
 3,
 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        long n = Long.parseLong(sc.nextLine().trim());\n        // TODO: write your solution\n    }\n}\n',
 'n = int(input())\n# TODO: write your solution\n',
 '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    long long n; cin >> n;\n    // TODO: write your solution\n    return 0;\n}\n',
 'const n = parseInt(require(\"fs\").readFileSync(0, \"utf8\").trim());\n// TODO: write your solution\n',
 2000, 256, NOW()
WHERE NOT EXISTS (SELECT 1 FROM problems WHERE title = 'Nth Fibonacci Number');

INSERT INTO problem_tags (problem_id, tag)
SELECT p.id, t.tag FROM problems p
JOIN (SELECT 'Dynamic Programming' AS tag UNION SELECT 'Math') t
WHERE p.title = 'Nth Fibonacci Number'
  AND NOT EXISTS (SELECT 1 FROM problem_tags pt WHERE pt.problem_id = p.id AND pt.tag = t.tag);

INSERT INTO test_cases (problem_id, input, expected_output, is_hidden, display_order)
SELECT p.id, tc.input, tc.expected_output, tc.is_hidden, tc.display_order
FROM problems p
JOIN (
  SELECT '10' AS input, '55' AS expected_output, FALSE AS is_hidden, 0 AS display_order
  UNION ALL SELECT '0', '0', FALSE, 1
  UNION ALL SELECT '1', '1', TRUE, 2
  UNION ALL SELECT '50', '12586269025', TRUE, 3
) tc
WHERE p.title = 'Nth Fibonacci Number'
  AND NOT EXISTS (SELECT 1 FROM test_cases t WHERE t.problem_id = p.id AND t.display_order = tc.display_order);

-- ---------------------------------------------------------------------------
-- Demo contest: currently ongoing (started yesterday, ends in 6 days) so it's
-- immediately testable after a fresh seed. Organization name is a placeholder —
-- edit/replace via the admin Contests panel.
-- ---------------------------------------------------------------------------
INSERT INTO contests (title, organization_name, description, start_time, end_time, is_published, created_by_user_id, created_at)
SELECT
 'Weekly Practice Contest #1',
 'CodeBench Community',
 '## Weekly Practice Contest\n\nSolve as many problems as you can. Scoring is points-based with a time penalty: your score is the sum of points for every problem you solve, and ties are broken by total time taken (plus 10 penalty minutes per wrong attempt on a problem before you get it right).\n\nGood luck!',
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_ADD(NOW(), INTERVAL 6 DAY),
 TRUE,
 (SELECT id FROM users WHERE username = 'admin'),
 NOW()
WHERE NOT EXISTS (SELECT 1 FROM contests WHERE title = 'Weekly Practice Contest #1');

INSERT INTO contest_problems (contest_id, problem_id, points, display_order)
SELECT c.id, p.id, cp.points, cp.display_order
FROM contests c
JOIN (
  SELECT 'Two Sum' AS title, 100 AS points, 0 AS display_order
  UNION ALL SELECT 'Reverse String', 100, 1
  UNION ALL SELECT 'Nth Fibonacci Number', 200, 2
) cp ON TRUE
JOIN problems p ON p.title = cp.title
WHERE c.title = 'Weekly Practice Contest #1'
  AND NOT EXISTS (SELECT 1 FROM contest_problems x WHERE x.contest_id = c.id AND x.problem_id = p.id);
