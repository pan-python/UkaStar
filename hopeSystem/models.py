class User:
    def __init__(self, id, name, student_id, avatar, total_points, created_at, updated_at):
        self.id = id
        self.name = name
        self.student_id = student_id
        self.avatar = avatar
        self.total_points = total_points
        self.created_at = created_at
        self.updated_at = updated_at

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'student_id': self.student_id,
            'avatar': self.avatar,
            'total_points': self.total_points,
            'created_at': str(self.created_at),
            'updated_at': str(self.updated_at)
        }

class PointCategory:
    def __init__(self, id, name, icon, color, description):
        self.id = id
        self.name = name
        self.icon = icon
        self.color = color
        self.description = description

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'icon': self.icon,
            'color': self.color,
            'description': self.description
        }

class PointItem:
    def __init__(self, id, category_id, name, points, description):
        self.id = id
        self.category_id = category_id
        self.name = name
        self.points = points
        self.description = description

    def to_dict(self):
        return {
            'id': self.id,
            'category_id': self.category_id,
            'name': self.name,
            'points': self.points,
            'description': self.description
        }

class DeductionCategory:
    def __init__(self, id, name, icon, color, description):
        self.id = id
        self.name = name
        self.icon = icon
        self.color = color
        self.description = description

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'icon': self.icon,
            'color': self.color,
            'description': self.description
        }

class DeductionItem:
    def __init__(self, id, category_id, name, points, description):
        self.id = id
        self.category_id = category_id
        self.name = name
        self.points = points
        self.description = description

    def to_dict(self):
        return {
            'id': self.id,
            'category_id': self.category_id,
            'name': self.name,
            'points': self.points,
            'description': self.description
        }

class RewardItem:
    def __init__(self, id, name, points, description, tag, is_custom):
        self.id = id
        self.name = name
        self.points = points
        self.description = description
        self.tag = tag
        self.is_custom = is_custom

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'points': self.points,
            'description': self.description,
            'tag': self.tag,
            'is_custom': self.is_custom
        }

class PointRecord:
    def __init__(self, id, user_id, type, item_id, custom_name, custom_points, points, description, status, created_at):
        self.id = id
        self.user_id = user_id
        self.type = type  # 1:加分, 2:减分, 3:兑换
        self.item_id = item_id
        self.custom_name = custom_name
        self.custom_points = custom_points
        self.points = points
        self.description = description
        self.status = status
        self.created_at = created_at

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'type': self.type,
            'item_id': self.item_id,
            'custom_name': self.custom_name,
            'custom_points': self.custom_points,
            'points': self.points,
            'description': self.description,
            'status': self.status,
            'created_at': str(self.created_at)
        }