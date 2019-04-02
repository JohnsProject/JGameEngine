bl_info = {
	"name": "Scene format (.scene)",
	"author": "John´s Project",
	"version": (0, 2),
	"blender": (2, 57, 0),
	"location": "File > Import-Export > Scene (.scene) ",
	"description": "Import-Export Scene",
	"warning": "",
	"wiki_url": "",
	"category": "Import-Export",
}

if "bpy" in locals():
	import importlib
	if "import_som" in locals():
		importlib.reload(import_som)
	if "export_som" in locals():
		importlib.reload(export_som)
else:
	import bpy

from bpy.props import StringProperty, BoolProperty
from bpy_extras.io_utils import ExportHelper


class SOMImporter(bpy.types.Operator):
	"""Load Scene Object Model data"""
	bl_idname = "import_model.scene"
	bl_label = "Import Scene"
	bl_options = {'UNDO'}

	filepath = StringProperty(
			subtype='FILE_PATH',
			)
	filter_glob = StringProperty(default="*.scene", options={'HIDDEN'})

	def execute(self, context):
		from . import import_som
		import_som.read(self.filepath)
		return {'FINISHED'}

	def invoke(self, context, event):
		wm = context.window_manager
		wm.fileselect_add(self)
		return {'RUNNING_MODAL'}


class SOMExporter(bpy.types.Operator, ExportHelper):
	"""Save Scene Object Model data"""
	bl_idname = "export_model.scene"
	bl_label = "Export Scene"

	filename_ext = ".scene"
	filter_glob = StringProperty(default="*.scene", options={'HIDDEN'})

	def execute(self, context):
		from . import export_som
		export_som.write(self.filepath)

		return {'FINISHED'}


def menu_import(self, context):
	self.layout.operator(SOMImporter.bl_idname, text="Scene (.scene)")


def menu_export(self, context):
	self.layout.operator(SOMExporter.bl_idname, text="Scene (.scene)")


def register():
	bpy.utils.register_module(__name__)

	bpy.types.INFO_MT_file_import.append(menu_import)
	bpy.types.INFO_MT_file_export.append(menu_export)


def unregister():
	bpy.utils.unregister_module(__name__)

	bpy.types.INFO_MT_file_import.remove(menu_import)
	bpy.types.INFO_MT_file_export.remove(menu_export)

if __name__ == "__main__":
	register()
