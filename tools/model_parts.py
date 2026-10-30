"""Resolve bottle membership from Blockbench groups, independent of cube count."""
def bottle_parts(model,count):
    elements=model['elements']
    if count==1:return [elements]
    def ids(node):
        if isinstance(node,str):return [node]
        return [uuid for child in node.get('children',[]) for uuid in ids(child)]
    groups=[set(ids(node)) for node in model.get('outliner',[]) if isinstance(node,dict)]
    if len(groups)!=count:raise ValueError(f'Expected {count} bottle groups, got {len(groups)}')
    parts=[[element for element in elements if element['uuid'] in group] for group in groups]
    flat=[element['uuid'] for part in parts for element in part]
    if any(not part for part in parts) or len(flat)!=len(elements) or len(set(flat))!=len(elements):
        raise ValueError('Bottle groups must cover every element exactly once')
    return parts
