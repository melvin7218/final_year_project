-- Add member_id column to meal_plans table
ALTER TABLE `meal_plans` ADD COLUMN `member_id` int(11) DEFAULT NULL AFTER `user_id`;

-- Add foreign key constraint
ALTER TABLE `meal_plans` ADD CONSTRAINT `fk_meal_plans_member` 
FOREIGN KEY (`member_id`) REFERENCES `user_members` (`member_id`) ON DELETE CASCADE;

-- Update existing meal plans to have member_id = NULL (for backward compatibility)
-- This allows existing meal plans to continue working 