CQ (AEM) Brainstorm
===================

The Main Goal of this Project is to have a representative playground for prototyping/brainstorming inside CQ (AEM) platform, produce useful knowledge and reusable artefacts. Please find a current vision of Project evolution in the RoadMap section below.

# RoadMap
- [Х] Custom Authentication Module
- [X] Custom Serialization Servlet
- [ ] A unit testing solution that enables creation and manipulation of CRX content in memory. Aimed to minimise efforts for the test content initialisation keeping focus on the minimal test data rather than comprehensive content tree representation.

# Installation

```bash
cd cq-brainstorm
maven clean install
cd cq-brainstorm-all
# Recheck connection settings and install all the staff on your server
maven install -P auto-deploy 
```
